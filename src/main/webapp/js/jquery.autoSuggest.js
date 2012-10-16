 /*
 * AutoSuggest
 * Copyright 2009-2010 Drew Wilson
 * www.drewwilson.com
 * code.drewwilson.com/entry/autosuggest-jquery-plugin
 *
 * Version 1.4   -   Updated: Mar. 23, 2010
 *
 * This Plug-In will auto-complete or auto-suggest completed search queries
 * for you as you type. You can add multiple selections and remove them on
 * the fly. It supports keybord navigation (UP + DOWN + RETURN), as well
 * as multiple AutoSuggest fields on the same page.
 *
 * Inspied by the Autocomplete plugin by: J�rn Zaefferer
 * and the Facelist plugin by: Ian Tearle (iantearle.com)
 *
 * This AutoSuggest jQuery plug-in is dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 */

(function($){
	$.fn.autoSuggest = function(data, options) {
		var defaults = { 
			asHtmlID: false,
			startText: "Enter Name Here",
			emptyText: "No Results Found",
			preFill: {},
			limitText: "No More Selections Are Allowed",
			selectedItemProp: "value", //name of object property
			selectedValuesProp: "value", //name of object property
			searchObjProps: "value", //comma separated list of object property names
			queryParam: "q",
			retrieveLimit: false, //number for 'limit' param on ajax request
			extraParams: "",
			matchCase: false,
			minChars: 1,
			keyDelay: 400,
			resultsHighlight: true,
			neverSubmit: false,
			selectionLimit: false,
			showResultList: true,
		  	start: function(){},
		  	selectionClick: function(elem){},
		  	selectionAdded: function(elem){},
		  	selectionRemoved: function(elem){ elem.remove(); },
		  	formatList: false, //callback function
		  	beforeRetrieve: function(string){ return string; },
		  	retrieveComplete: function(data){ return data; },
		  	resultClick: function(data){},
		  	resultsComplete: function(){},
		  	addByWrite: true
	  	};  
	 	var opts = $.extend(defaults, options);	 	
		
		var d_type = "object";
		var d_count = 0;
		if(typeof data == "string") {
			d_type = "string";
			var req_string = data;
		} else {
			var org_data = data;
			for (k in data) if (data.hasOwnProperty(k)) d_count++;
		}
		if((d_type == "object" && d_count > 0) || d_type == "string"){
			return this.each(function(x){
				if(!opts.asHtmlID){
					x = x+""+Math.floor(Math.random()*100); //this ensures there will be unique IDs on the page if autoSuggest() is called multiple times
					var x_id = "as-input-"+x;
				} else {
					x = opts.asHtmlID;
					var x_id = x;
				}
				opts.start.call(this);
				var input = $(this);
				input.attr("autocomplete","off").addClass("as-input").attr("id",x_id).val(opts.startText);
				var input_focus = false;
				
				// Setup basic elements and render them to the DOM
				input.wrap('<ul class="as-selections" id="as-selections-'+x+'"></ul>').wrap('<li class="as-original" id="as-original-'+x+'"></li>');
				var selections_holder = $("#as-selections-"+x);
				var org_li = $("#as-original-"+x);				
				var results_holder = $('<div class="as-results" id="as-results-'+x+'"></div>').hide();
				var results_ul =  $('<ul class="as-list"></ul>');
				var values_input = $('<input type="hidden" class="as-values" name="as_values_'+x+'" id="as-values-'+x+'" />');
				var prefill_value = "";
				
				$(results_ul).mousedown(function (event) { // fix for the scroll bar
					event.stopPropagation();
					event.preventDefault();
					return false;
				});
				
				input.values_input = values_input;
				input.opts = opts;
				
				if(typeof opts.preFill == "string"){
					var vals = opts.preFill.split(",");					
					for(var i=0; i < vals.length; i++){
						var v_data = {};
						v_data[opts.selectedValuesProp] = vals[i];
						if(vals[i] != ""){
							add_selected_item(v_data, "000"+i);	
						}		
					}
					prefill_value = opts.preFill;
				} else {
					prefill_value = "";
					var prefill_count = 0;
					for (k in opts.preFill) if (opts.preFill.hasOwnProperty(k)) prefill_count++;
					if(prefill_count > 0){
						for(var i=0; i < prefill_count; i++){
							var new_v = opts.preFill[i][opts.selectedValuesProp];
							if(new_v == undefined){ new_v = ""; }
							prefill_value = prefill_value+new_v+",";
							if(new_v != ""){
								add_selected_item(opts.preFill[i], "000"+i);	
							}		
						}
					}
				}
				if(prefill_value != ""){
					input.val("");
					var lastChar = prefill_value.substring(prefill_value.length-1);
					if(lastChar != ","){ prefill_value = prefill_value+","; }
					values_input.val(","+prefill_value);
					$("li.as-selection-item", selections_holder).addClass("blur").removeClass("selected");
				}
				input.after(values_input);
				selections_holder.click(function(){
					input_focus = true;
					input.focus();
				}).mousedown(function(){ input_focus = false; }).after(results_holder);	

				var timeout = null;
				var prev = "";
				var totalSelections = 0;
				var tab_press = false;
				
				var blurWidth = '320px';
				
				$(input).css('width', blurWidth);
				
				// Handle input field events
				input.focus(function(){	
					input_focus = true;
					if($(this).val() == opts.startText && values_input.val() == ""){
						$(this).val("");
						$(this).css("width", "200px");
					} else if(input_focus){
						$("li.as-selection-item", selections_holder).removeClass("blur");
						if($(this).val() != ""){
							results_ul.css("width",selections_holder.outerWidth());
							results_holder.show();
						}
					}
					input_focus = true;
					return true;
				}).blur(function(){
					$(this).css("width", blurWidth);
					if($(this).val() == "" && values_input.val() == "" && prefill_value == ""){
						$(this).val(opts.startText);
						$(this).css('width', blurWidth);
					} else if(input_focus){
						$("li.as-selection-item", selections_holder).addClass("blur").removeClass("selected");
						results_holder.hide();
					}				
				}).keydown(function(e) {
					// track last key pressed
					lastKeyPressCode = e.keyCode;
					first_focus = false;
					switch(e.keyCode) {
						case 38: // up
							e.preventDefault();
							moveSelection("up");
							break;
						case 40: // down
							e.preventDefault();
							moveSelection("down");
							break;
						case 8:  // delete
							if(input.val() == ""){							
								var last = values_input.val().split(",");
								last = last[last.length - 2];
								selections_holder.children().not(org_li.prev()).removeClass("selected");
								
								values_input.val(values_input.val().replace(last+",",""));
								opts.selectionRemoved.call(this, org_li.prev(), {label: $('<div/>').text(org_li.prev().text().substring(1)).html(), value: last});
							}
							if(input.val().length == 1){
								results_holder.hide();
								 prev = "";
							}
							if($(":visible",results_holder).length > 0){
								if (timeout){ clearTimeout(timeout); }
								timeout = setTimeout(function(){ keyChange(); }, opts.keyDelay);
							}
							break;
						case 9: case 188:  // tab or comma
							if (!opts.addByWrite) {
								e.preventDefault();
								return;
							}
							
							tab_press = true;
							var i_input = input.val().replace(/(,)/g, "");
							if(i_input != "" && values_input.val().search(i_input+",") < 0 && i_input.length >= opts.minChars){	
								e.preventDefault();
								var n_data = {};
								n_data[opts.selectedItemProp] = i_input;
								n_data[opts.selectedValuesProp] = i_input;																				
								var lis = $("li", selections_holder).length;
								add_selected_item(n_data, "00"+(lis+1));
								input.val("");
							}
						case 13: // return
							tab_press = false;
							var active = $("li.active:first", results_holder);
							if(active.length > 0){
								active.click();
								results_holder.hide();
							}
							if(opts.neverSubmit || active.length > 0){
								e.preventDefault();
							}
							break;
						default:
							if(opts.showResultList){
								if(opts.selectionLimit && $("li.as-selection-item", selections_holder).length >= opts.selectionLimit){
									results_ul.html('<li class="as-message">'+opts.limitText+'</li>');
									results_holder.show();
								} else {
									if (timeout){ clearTimeout(timeout); }
									timeout = setTimeout(function(){ keyChange(); }, opts.keyDelay);
								}
							}
							break;
					}
				});
				
				input.change(function (e) {
					var val = $(input).val();
					var lastCh = val.substring(val.length - 1);
					if (lastCh == '|') {
						var i_input = input.val().replace(/(\|)/g, "");
						var labelTypeV = i_input.split(':');
						i_input = labelTypeV[0];
						var type = labelTypeV[1];
						if (values_input.val().search(new RegExp("^" + i_input + ",|" + "," + i_input + ",", "g")) >= 0) {
							$(input).val('');
							return;
						}
						if(i_input != "" && values_input.val().search(i_input+",") < 0 && i_input.length >= opts.minChars){	
							e.preventDefault();
							var n_data = {};
							n_data[opts.selectedItemProp] = i_input;
							n_data[opts.selectedValuesProp] = i_input;	
							n_data.type = type;
							
							if (labelTypeV.length > 2)
								n_data.tooltip = labelTypeV[2];
							
							var lis = $("li", selections_holder).length;
							add_selected_item(n_data, "00"+(lis+1));
							input.val("");
						}
					}
					
					if (lastCh == ',' || lastCh == '\t') {
						if (!opts.addByWrite) {
							// remove the last comma or tab
							if (e.keyCode == 188) {	// comma
								input.val(input.val().replace(/(,)/g, ""));
							}
							e.preventDefault();
							return;
						}
						
						tab_press = true;
						var i_input = input.val().replace(/(,)/g, "");
						if(i_input != "" && values_input.val().search(i_input+",") < 0 && i_input.length >= opts.minChars){	
							e.preventDefault();
							var n_data = {};
							n_data[opts.selectedItemProp] = i_input;
							n_data[opts.selectedValuesProp] = i_input;																				
							var lis = $("li", selections_holder).length;
							add_selected_item(n_data, "00"+(lis+1));
							input.val("");
						}
					}
				});
				
				function keyChange() {
					// ignore if the following keys are pressed: [del] [shift] [capslock]
					if( lastKeyPressCode == 46 || (lastKeyPressCode > 8 && lastKeyPressCode < 32) ){ return results_holder.hide(); }
					var string = input.val().replace(/[\\]+|[\/]+/g,"");
					if (string == prev) return;
					prev = string;
					if (string.length >= opts.minChars) {
						selections_holder.addClass("loading");
						if(d_type == "string"){
							var limit = "";
							if(opts.retrieveLimit){
								limit = "&limit="+encodeURIComponent(opts.retrieveLimit);
							}
							if(opts.beforeRetrieve){
								string = opts.beforeRetrieve.call(this, string);
							}
							$.getJSON(req_string+"?"+opts.queryParam+"="+encodeURIComponent(string)+limit+opts.extraParams, function(data){ 
								d_count = 0;
								var new_data = opts.retrieveComplete.call(this, data);
								for (k in new_data) if (new_data.hasOwnProperty(k)) d_count++;
								processData(new_data, string); 
							});
						} else {
							if(opts.beforeRetrieve){
								string = opts.beforeRetrieve.call(this, string);
							}
							processData(org_data, string);
						}
					} else {
						selections_holder.removeClass("loading");
						results_holder.hide();
					}
				}
				var num_count = 0;
				function processData(data, query){
					if (!opts.matchCase){ query = query.toLowerCase(); }
					var matchCount = 0;
					results_holder.html(results_ul.html("")).hide();
					for(var i=0;i<d_count;i++){				
						var num = i;
						num_count++;
						var forward = false;
						if(opts.searchObjProps == "value") {
							var str = data[num].value;
						} else {	
							var str = "";
							var names = opts.searchObjProps.split(",");
							for(var y=0;y<names.length;y++){
								var name = $.trim(names[y]);
								str = str+data[num][name]+" ";
							}
						}
						if(str){
							if (!opts.matchCase){ str = str.toLowerCase(); }				
							if(str.search(query) != -1 && values_input.val().search(","+data[num][opts.selectedValuesProp]+",") == -1){
								forward = true;
							}
						}
						if(forward){
							var formatted = $('<li class="as-result-item ' + data[i].type + '" id="as-result-item-'+num+'"></li>').click(function(){
									var raw_data = $(this).data("data");
									var number = raw_data.num;
									if($("#as-selection-"+number, selections_holder).length <= 0 && !tab_press){
										var data = raw_data.attributes;
										input.val("").focus();
										prev = "";
										add_selected_item(data, number);
										opts.resultClick.call(this, raw_data);
										results_holder.hide();
									}
									tab_press = false;
								}).mousedown(function(){ input_focus = false; }).mouseover(function(){
									$("li", results_ul).removeClass("active");
									$(this).addClass("active");
								}).data("data",{attributes: data[num], num: num_count});
							var this_data = $.extend({},data[num]);
							if (!opts.matchCase){ 
								var regx = new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + query + ")(?![^<>]*>)(?![^&;]+;)", "gi");
							} else {
								var regx = new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + query + ")(?![^<>]*>)(?![^&;]+;)", "g");
							}
							
							if(opts.resultsHighlight){
								this_data[opts.selectedItemProp] = this_data[opts.selectedItemProp].replace(regx,"<em>$1</em>");
							}
							
							if(!opts.formatList){
								formatted = formatted.html(this_data[opts.selectedItemProp]);
							} else {
								formatted = formatted.html(this_data[opts.selectedItemProp]);
								formatted = opts.formatList.call(this, this_data, formatted);	
							}
							results_ul.append(formatted);
							delete this_data;
							matchCount++;
							if(opts.retrieveLimit && opts.retrieveLimit == matchCount ){ break; }
						}
					}
					selections_holder.removeClass("loading");
					if(matchCount <= 0){
						results_ul.html('<li class="as-message">'+opts.emptyText+'</li>');
					}
					results_ul.css("width", selections_holder.outerWidth());
					results_holder.show();
					opts.resultsComplete.call(this);
				}
				
				function add_selected_item(data, num){
					if (data.type == null)
						data.type = 'keyword';
					
					values_input.val(values_input.val()+data[opts.selectedValuesProp]+",");
					var item = $('<li class="as-selection-item ' + data.type + '" id="as-selection-'+num+'"></li>').click(function(){
							opts.selectionClick.call(this, $(this), input);
							selections_holder.children().removeClass("selected");
							$(this).addClass("selected");
						}).mousedown(function(){ input_focus = false; });
					var close = $('<a class="as-close">&times;</a>').click(function(){
							values_input.val(values_input.val().replace(data[opts.selectedValuesProp]+",",""));
							opts.selectionRemoved.call(this, item, data);
							input_focus = true;
							input.focus();
							return false;
						});
					
					item.data = data;
					
					org_li.before(item.html(data[opts.selectedItemProp]).prepend(close));
					opts.selectionAdded.call(this, org_li.prev(), data);
				}
				
				function moveSelection(direction){
					if($(":visible",results_holder).length > 0){
						var lis = $("li", results_holder);
						if(direction == "down"){
							var start = lis.eq(0);
						} else {
							var start = lis.filter(":last");
						}					
						var active = $("li.active:first", results_holder);
						if(active.length > 0){
							if(direction == "down"){
							start = active.next();
							} else {
								start = active.prev();
							}	
						}
						lis.removeClass("active");
						start.addClass("active");
					}
				}				
			});
		}
	};
})(jQuery);  	