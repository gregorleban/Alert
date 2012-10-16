var monthLengths = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
var defaultDateFormat = "isoDate";

var settingManually = false;
var currentTab = 0;

/**
 * Returns an URL containing the current state of the interface.
 */
function genCurrentUrl() {
	// collect all the parameters
	var state = getCurrentState();
	var result = '';
	var indexPage = 'index.xhtml';
	
	if (state != null)
		result = window.location.pathname.indexOf(indexPage) < 0 ? window.location.pathname + indexPage + '?' + $.param(state) : window.location.pathname + '?' + $.param(state);
	else
		result = window.location.pathname.replace(indexPage, '');
	
	if (currentTab != 0)
		result += result.indexOf(indexPage) < 0 ? indexPage + '#' + currentTab : '#' + currentTab;
	
	return result;
}

/**
 * Returns an object which represents the current state of the UI.
 */
function getCurrentState() {
	var searchGeneral = viz.searchStateGeneral;
	var searchPerson = viz.searchStatePerson;	// TODO
	
	var result = {};
	var general = {};
	var duplicate = {};
	var myCode = {};
	
	
	// general search
	// search terms
	var people = searchGeneral.getTypeV('person');
	var keywords = $('#keyword_text').val();
	var sources = searchGeneral.getTypeV('source');
	var products = searchGeneral.getTypeV('product');
	var issues = searchGeneral.getTypeV('issue');
	
	
	
	if (people.length > 0) general.people = people;
	if (keywords.length > 0) general.keywords = keywords;
	if (sources.length > 0) general.sources = sources;
	if (products.length > 0) general.products = products;
	if (issues.length > 0) general.issues = issues;
	
	// checkboxes
	var issueChk = $('#issues_check').attr('checked') == 'checked';
	var commitsChk = $('#commits_check').attr('checked') == 'checked';
	var forumsChk = $('#forums_check').attr('checked') == 'checked';
	var mailingChk = $('#mailing_check').attr('checked') == 'checked';
	var wikisChk = $('#wikis_check').attr('checked') == 'checked';
	
	var genOpen = $('#gen_open_check').attr('checked') == 'checked';
	var genNone = $('#gen_none_check').attr('checked') == 'checked';
	var genVerified = $('#gen_verified_check').attr('checked') == 'checked';
	var genFixed = $('#gen_fixed_check').attr('checked') == 'checked';
	var genAssigned = $('#gen_assigned_check').attr('checked') == 'checked';
	var genWont = $('#gen_wont_check').attr('checked') == 'checked';
	var genResolved = $('#gen_resolved_check').attr('checked') == 'checked';
	var genInvalid = $('#gen_invalid_check').attr('checked') == 'checked';
	var genClosed = $('#gen_closed_check').attr('checked') == 'checked';
	var genWorks = $('#gen_works_check').attr('checked') == 'checked';
	var genUnknown = $('#gen_unknown_check').attr('checked') == 'checked';
	var genDuplicate = $('#gen_duplicate_check').attr('checked') == 'checked';
	
	if (!issueChk) general.is = false;
	if (!commitsChk) general.c = false;
	if (!forumsChk) general.fi = false;
	if (!mailingChk) general.m = false;
	if (!wikisChk) general.wo = false;
	
	if (!genOpen) general.go = false;
	if (!genNone) general.gn = false;
	if (!genVerified) general.gv = false;	
	if (!genFixed) general.gf = false;
	if (!genAssigned) general.ga = false;
	if (!genWont) general.gw = false;
	if (!genResolved) general.gr = false;
	if (!genInvalid) general.gi = false;
	if (!genClosed) general.gc = false;
	if (!genWorks) general.gwo = false;
	if (!genUnknown) general.gu = false;
	if (!genDuplicate) general.gd = false;
	
	// dates
	var fromDate = $('#from_text').val();
	var toDate = $('#to_text').val();
	
	if (fromDate != null && fromDate != '') general.from = fromDate;
	if (toDate != null && toDate != '') general.to = toDate;
	
	// search options
	var sort = $('input[name=sort_radio]:checked').val();
	if (sort == 'date')
		general.sort = 'date';
	
	var orSearch = $('#use_or_check').attr('checked') == 'checked';
	if (orSearch)
		general.or = true;
	
	// duplicate issue
	var issueId = $('#issue_id_text').val();
	if (issueId.length > 0) duplicate.iid = issueId;
	
	var duplNoneChk = $('#dup_none_check').attr('checked') == 'checked';
	var duplFixedChk = $('#dup_fixed_check').attr('checked') == 'checked';
	var duplWontChk = $('#dup_wont_check').attr('checked') == 'checked';
	var duplInvalidChk = $('#dup_invalid_check').attr('checked') == 'checked';
	var duplDuplicateChk = $('#dup_duplicate_check').attr('checked') == 'checked';
	var duplWorksChk = $('#dup_works_check').attr('checked') == 'checked';
	var duplUnknownChk = $('#dup_unknown_check').attr('checked') == 'checked';
	var duplOpenChk = $('#dup_open_check').attr('checked') == 'checked';
	var duplVerifiedChk = $('#dup_veririfed_check').attr('checked') == 'checked';
	var duplAssignedChk = $('#dup_assigned_check').attr('checked') == 'checked';
	var duplResolvedChk = $('#dup_resolved_check').attr('checked') == 'checked';
	var duplClosedChk = $('#dup_closed_check').attr('checked') == 'checked';
	
	if (!duplNoneChk) duplicate.n = false;
	if (!duplFixedChk) duplicate.f = false;
	if (!duplWontChk) duplicate.w = false;
	if (!duplInvalidChk) duplicate.i = false;
	if (!duplDuplicateChk) duplicate.d = false;
	if (!duplWorksChk) duplicate.wo = false;
	if (!duplUnknownChk) duplicate.u = false;
	if (!duplOpenChk) duplicate.o = false;
	if (!duplVerifiedChk) duplicate.v = false;
	if (!duplAssignedChk) duplicate.a = false;
	if (!duplResolvedChk) duplicate.r = false;
	if (!duplClosedChk) duplicate.c = false;
	
	// issues related to my code
	var myNoneChk = $('#my_none_check').attr('checked') == 'checked';
	var myFixedChk = $('#my_fixed_check').attr('checked') == 'checked';
	var myWontChk = $('#my_wont_check').attr('checked') == 'checked';
	var myInvalidChk = $('#my_invalid_check').attr('checked') == 'checked';
	var myDuplicateChk = $('#my_duplicate_check').attr('checked') == 'checked';
	var myWorksChk = $('#my_works_check').attr('checked') == 'checked';
	var myUnknownChk = $('#my_unknown_check').attr('checked') == 'checked';
	var myOpenChk = $('#my_open_check').attr('checked') == 'checked';
	var myVerifiedChk = $('#my_veririfed_check').attr('checked') == 'checked';
	var myAssignedChk = $('#my_assigned_check').attr('checked') == 'checked';
	var myResolvedChk = $('#my_resolved_check').attr('checked') == 'checked';
	var myClosedChk = $('#my_closed_check').attr('checked') == 'checked';
	
	if (!myNoneChk) myCode.n = false;
	if (!myFixedChk) myCode.f = false;
	if (!myWontChk) myCode.w = false;
	if (!myInvalidChk) myCode.i = false;
	if (!myDuplicateChk) myCode.d = false;
	if (!myWorksChk) myCode.wo = false;
	if (!myUnknownChk) myCode.u = false;
	if (!myOpenChk) myCode.o = false;
	if (!myVerifiedChk) myCode.v = false;
	if (!myAssignedChk) myCode.a = false;
	if (!myResolvedChk) myCode.r = false;
	if (!myClosedChk) myCode.c = false;
	
	// construct result
	if (Object.getOwnPropertyNames(general).length > 0) result.gen = general;
	if (Object.getOwnPropertyNames(duplicate).length > 0) result.dupl = duplicate;
	if (Object.getOwnPropertyNames(myCode).length > 0) result.mc = myCode;

	return Object.getOwnPropertyNames(result).length === 0 ? null : result;
}

function updateUrl(event) {
	if (!settingManually) {
		var url = genCurrentUrl();
		var state = getCurrentState();
		
		history.replaceState(state, '', url);
	}
	
	return false;
}

/**
 * Decodes the current URL and returns a parsed object.
 */
function decodeUrl() {
	var params = window.location.search.substring(1);
	var tab = window.location.hash == '' ? 0 : parseInt(window.location.hash.substring(1));
	
	if (params == '' && tab == 0) 
		return null;
	else
		return {params: params == '' ? null : $.deparam(params), tab: tab};
}

function uncheckByAttr(attribute, prefix) {
	var selector = '#' + prefix;
	switch(attribute) {
	case 'n':
		selector += 'none_check';
		break;
	case 'f':
		selector += 'fixed_check';
		break;
	case 'w':
		selector += 'wont_check';
		break;
	case 'i':
		selector += 'invalid_check';
		break;
	case 'd':
		selector += 'duplicate_check';
		break;
	case 'wo':
		selector += 'works_check';
		break;
	case 'u':
		selector += 'unknown_check';
		break;
	case 'o':
		selector += 'open_check';
		break;
	case 'v':
		selector += 'veririfed_check';
		break;
	case 'a':
		selector += 'assigned_check';
		break;
	case 'r':
		selector += 'resolved_check';
		break;
	case 'c':
		selector += 'closed_check';
		break;
	};
	
	$(selector).attr('checked', false);
}

/**
 * Parses the state of the UI from the URL and updates the UI.
 */
function loadState() {
	var state = decodeUrl();
	if (state == null) return;
	
	var params = state.params;
	var tab = state.tab;
	
	var general = params.gen;
	var duplicate = params.dupl;
	var myCode = params.mc;
	
	settingManually = true;
	
	// general search
	var searchTerms = {people: true, concepts: true, sources: true, products: true, issues: true};
	var filterChks = {is: true, c: true, fi: true, m: true, wo: true};
	var issueChks = {n: true, f: true, w: true, i: true, d: true, wo: true, u: true, o: true, v: true, a: true, r: true, c: true};
	var issueChksGen = {go: true, gn: true, gv: true, gf: true, ga: true, gw: true, gr: true, gi: true, gc: true, gwo: true, gu: true, gd: true};
	
	var dates = {from: true, to: true};
	
	if (general != null) {
		// go through all the properties
		for (var attribute in general) {
			var value = general[attribute];
			
			if (searchTerms[attribute]) {	// search terms
				// the value is an array of search terms
				for (var i = 0; i < value.length; i++)
					viz.addToSearchField('other_text', value[i]);
			}
			else if (attribute == 'keywords') {
				$('#keyword_text').val(value);
			}
			else if(filterChks[attribute] || issueChksGen[attribute]) {	// checkboxes
				var selector = null;
				switch(attribute) {
				case 'is':
					selector = '#issues_check';
					break;
				case 'c':
					selector = '#commits_check';
					break;
				case 'fi':
					selector = '#forums_check';
					break;
				case 'm':
					selector = '#mailing_check';
					break;
				case 'wo':
					selector = '#wikis_check';
					break;
				case 'go':
					selector = '#gen_open_check';
					break;
				case 'gn':
					selector = '#gen_none_check';
					break;
				case 'gv':
					selector = '#gen_verified_check';
					break;
				case 'gf':
					selector = '#gen_fixed_check';
					break;
				case 'ga':
					selector = '#gen_assigned_check';
					break;
				case 'gw':
					selector = '#gen_wont_check';
					break;
				case 'gr':
					selector = '#gen_resolved_check';
					break;
				case 'gi':	
					selector = '#gen_invalid_check';
					break;
				case 'gc':
					selector = '#gen_closed_check';
					break;
				case 'gwo':
					selector = '#gen_works_check';
					break;
				case 'gu':
					selector = '#gen_unknown_check';
					break;
				case 'gd':
					selector = '#gen_duplicate_check';
					break;
				}
				
				$(selector).attr('checked', false);
			} else if (dates[attribute]) {
				var field = attribute == 'from' ? 'from_text' : 'to_text';
				$('#' + field).val(value);
			} else if (attribute == 'sort') {
				if (value == 'date')
					$('#date_sort').attr('checked', 'checked');
			} else if (attribute == 'or') {
				$('#use_or_check').attr('checked', 'checked');
			};
		};
	}
	
	// duplicate issue
	if (duplicate != null) {
		for (var attribute in duplicate) {
			if (attribute == 'iid')
				$('#issue_id_text').val(duplicate.iid);
			else if (issueChks[attribute])
				uncheckByAttr(attribute, 'dup_');
		}
	}
	
	// issues related to my code
	if (myCode != null) {
		for (var attribute in myCode) {
			if (issueChks[attribute])
				uncheckByAttr(attribute, 'my_');
		}
	}
	
	$('#navigation').find('a')[tab].click();
	
	switch(tab) {
	case 0:
		viz.searchGeneral();
		break;
	case 1:
		viz.searchIssueId();
		break;
	case 2:
		break;
	case 3:
		break;
	}
	
	settingManually = false;
}

// helper function for drawing lines on the people graph
var intersect_line_box = function(p1, p2, boxTuple) {
	var p3 = {x:boxTuple[0], y:boxTuple[1]};
    var w = boxTuple[2];
    var h = boxTuple[3];

	var tl = {x: p3.x, y: p3.y};
	var tr = {x: p3.x + w, y: p3.y};
	var bl = {x: p3.x, y: p3.y + h};
	var br = {x: p3.x + w, y: p3.y + h};

	return intersect_line_line(p1, p2, tl, tr) ||
        intersect_line_line(p1, p2, tr, br) ||
        intersect_line_line(p1, p2, br, bl) ||
        intersect_line_line(p1, p2, bl, tl) ||
        false;
};
var intersect_line_line = function(p1, p2, p3, p4) {
	var denom = ((p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y));
	if (denom === 0) return false; // lines are parallel
	var ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x)) / denom;
	var ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x)) / denom;

	if (ua < 0 || ua > 1 || ub < 0 || ub > 1)  return false;
	return arbor.Point(p1.x + ua * (p2.x - p1.x), p1.y + ua * (p2.y - p1.y));
};


//returns the value of a CSS attribute
function getCssValue(clazz, attribute) {
	var $p = $("<p class='" + clazz + "'></p>").hide().appendTo("body");
	var value = $p.css(attribute);
	$p.remove();
	return value;
}

var SocialGraph = function(options){
	var width = $('#' + options.container).width();
	var height = $('#' + options.container).height();
	
	//read CSS attributes
	var selectedTextClr = getCssValue("selected-node", "color");
	var selectedBoxClr = getCssValue("selected-node", "background-color");
	var neighbourTextClr = getCssValue("neighbour-node", "color");
	var neighbourBoxClr = getCssValue("neighbour-node", "background-color");
	
	if (selectedTextClr == null) selectedTextClr = "yellow";
	if (selectedBoxClr == null) selectedBoxClr = "rgba(30, 116, 255, .6)";
	if (neighbourTextClr == null) neighbourTextClr = "white";
	if (neighbourBoxClr == null) selectedBoxClr = "rgba(62, 189, 255, .6)";
	
	// tooltip functions
	function showTooltip(data) {
		var html = '<table class="tooltip"><tbody>';
		html += '<tr>';
		html += '<td class="tooltip_name">' + data.label + '</td>';
		html += '<td class="tooltip_id">' + data.id + '</td>';
		html += '</tr>';
		
		html += '<tr>';
		html += '<td class="tooltip_mail">' + data.email + '</td>';
		html += '<td class="tooltip_expertise">Missing!!</td>';
		html +='</tr>';
		html += '</tbody></table>';
		
		tooltip.show(html);
	}
	
	function hideTooltip() {
		if (tooltip != null)
			tooltip.hide();
	}
	
	$('#' + options.container).mouseleave(hideTooltip);
	
	var that = {
		graph: DynamicGraph({
			container: options.container,
			width: width,
			height: height,
			draggable: true,
			selectionMode: 'single',
			startDisplayLevel: 50,
			step: options.step,
			
			drawNode: function (context, data) {
				var pos = data.pos;
				if (pos == null) return;
				
				var fontSize = data.size;
				var textColor = "black";
				var label = data.label;
				
				var margin = 8;
				context.font = fontSize + "px Helvetica";
				var width = context.measureText(label).width + 10;
				var height = fontSize + margin;
				
				var boxColor;
				if (data.selected) {
					boxColor = selectedBoxClr;
					textColor = selectedTextClr;
				} else if (data.neighboursSelected > 0) {
					boxColor = neighbourBoxClr;
					textColor = neighbourTextClr;
				} else {
					boxColor = 'rgba(255,255,255,0)';
				}
				
				var edgeRadius = Math.min(width, height)/4;
				context.save();
				context.beginPath();
					// draw the rect in the back
					context.fillStyle = boxColor;
					context.moveTo(-width/2 + edgeRadius, -height/2);
					context.lineTo(width/2 - edgeRadius, -height/2);
					context.arc(width/2 - edgeRadius, -height/2 + edgeRadius, edgeRadius, 1.5*Math.PI, 2*Math.PI, false);
					context.lineTo(width/2, height - edgeRadius);
					context.arc(width/2 - edgeRadius, height/2 - edgeRadius, edgeRadius, 0, .5*Math.PI, false);
					context.lineTo(-width/2 + edgeRadius, height/2);
					context.arc(-width/2 + edgeRadius, height/2 - edgeRadius, edgeRadius, .5*Math.PI, Math.PI, false);
					context.lineTo(-width/2, -height/2 + edgeRadius);
					context.arc(-width/2 + edgeRadius, -height/2 + edgeRadius, edgeRadius, Math.PI, 1.5*Math.PI, false);
					context.fill();
					
					// draw the label
					context.fillStyle = textColor;
					context.textAlign = 'center';
					context.textBaseline = 'middle';
					context.fillText(label, 0, 0);
				context.closePath();
				context.restore();
				
				data.nodeBox = [pos.x - width/2, pos.y - height/2, width, height];
			},
			drawEdge: function (context, data) {
				var sourcePos = data.pos1;
				var targetPos = data.pos2;
				
				if (sourcePos == null || targetPos == null) return;
				
				var minAlpha = .2;
				
				var alpha = data.count == 0 ? minAlpha : Math.max(.2, 1 - 1/data.count);
				var color = "rgba(85, 85, 85, " + alpha + ")";
								
				var tail = intersect_line_box(sourcePos, targetPos, data.source.nodeBox);
				var head = intersect_line_box(tail, targetPos, data.target.nodeBox);
				
				
				// draw the line
				context.save() ;
				context.beginPath();
					context.lineWidth = 1;
					context.strokeStyle = (color) ? color : "#cccccc";
					context.fillStyle = color;
	
					context.moveTo(tail.x, tail.y);
					context.lineTo(head.x, head.y);
					context.stroke();
				context.closePath();
				context.restore();
				
				// draw the arrow
				context.save();
					// move to the head position of the edge we just drew
					var wt = 1;
					var arrowLength = 6 + wt;
					var arrowWidth = 2 + wt;
					context.fillStyle = (color) ? color : "#cccccc";
					context.translate(head.x, head.y);
					context.rotate(Math.atan2(head.y - tail.y, head.x - tail.x));
	
					// delete some of the edge that's already there (so the point isn't hidden)
					context.clearRect(-arrowLength/2,-wt/2, arrowLength/2,wt);
	
					// draw the chevron
					context.beginPath();
					context.moveTo(-arrowLength, arrowWidth);
					context.lineTo(0, 0);
					context.lineTo(-arrowLength, -arrowWidth);
					context.lineTo(-arrowLength * 0.8, -0);
					context.closePath();
					context.fill();
				context.restore();
			},
			
			handlers: {
				node: {
					'dblclick': function (event, node) {
						event.cancelBubble = true;
						viz.addToSearchField('other_text', {type: 'person', label: node.data.label, value: node.data.email});
						node.select(true);
					},
					'mouseover': function (event, node) {
						if (node.data.mouseOver == true) return;	 // fix
						node.data.mouseOver = true;
						
						document.body.style.cursor = 'pointer';
						showTooltip(node.data);
					},
					'mouseout': function (event, node) {
						if (node.data.mouseOver != true) return;	// fix
						node.data.mouseOver = false;
						
						hideTooltip();
						
						document.body.style.cursor = 'default';
					},
					'mousemove': function (event) {
						showTooltip(node.data);
					},
					'mousedown': function (event) {
						hideTooltip();
					}
				},
				stage: {
					'mousedown': function () {
						hideTooltip();
					}
				}
			}
		}),
		
		showMore: function() {
			that.graph.showMore();
		},
		
		showLess: function() {
			that.graph.showLess();
		},
		
		clear: function () {
			that.graph.clear();
		},
		
		init: function (data) {
			that.graph.setData(data);
		},
		
		getHeight: function () {
			return that.graph.getHeight();
		},
		
		getWidth: function () {
			return that.graph.getWidth();
		},
		
		resize: function (newWidth, newHeight) {
			that.graph.setSize(newWidth, newHeight);
		}
	};
	
	return that;
};

var ZoomHistory = function () {
	var items = [];
	var current = {min: null, max: null};
	
	var that = {
		addItem: function (min, max) {
			// before it was if (current != null), but current is never null
			items.push(current);
			current = {min: min, max: max};
		},
		
		getPrevious: function () {
			if (items.length > 0) {
				current = items.pop();
				return current;
			} return null;
		},
		
		clear: function () {
			items = [];
			current = {min: null, max: null};
		},
		
		isEmpty: function () {
			return items.length == 0;
		}
	};
	
	return that;
};

var Search = function (opts) {
	var searchTerms = null;
	
	var that = {
		getObjByLabel: function (type, label) {
			var list = searchTerms[type];
			
			for (var i = 0; i < list.length; i++) {
				if (list[i].label == label)
					return list[i];
			}
			
			return null;
		},
			
		indexOfLabel: function (type, label) {
			var prV = searchTerms[type];
			for (var i = 0; i < prV.length; i++) {
				if (prV[i].label == label)
					return i;
				
				var labelV = prV[i].label.split(',');
				for (var j = 0; j < labelV.length; j++) {
					if (labelV[j] == label)
						return i;
				}
	    	}
	    	return -1;
		},
		
		containsLabel: function (type, label) {
			return that.indexOfLabel(type, label) >= 0;
		},
		
		addOrTerm: function (type, idx, data) {
			if (that.containsLabel(type, data.label)) return;
			
			var list = searchTerms[type];
			list[idx].label += ',' + data.label;
			list[idx].value += '|' + data.value;
		},
		
		removeTerm: function (type, data) {
			var idx = that.indexOfLabel(type, data.label);
			if (idx < 0) return;
			
			var list = searchTerms[type];
			list[idx].label = list[idx].label.replace(new RegExp('^' + data.label + '|,' + data.label, 'g'), '');
			list[idx].value = list[idx].value.replace(new RegExp('^' + data.value + '|\|' + data.value, 'g'), '');
    		
    		if (searchTerms[type][idx].label.length == 0)
    			searchTerms[type].splice(idx, idx + 1);
		},
		
		addToSearch: function (data) {
			if (data.type == 'product' || data.type == 'person' || data.type == 'issue') {
				// have to send URI
				var value = data.value;
				var label = data.label;
				
				var array = searchTerms[data.type];
				if (that.indexOfLabel(data.type, label) < 0)
					array.push({type: data.type, label: label, value: value});
			} else if (data.type == 'source') {
				var array = searchTerms[data.type];
				if (that.indexOfLabel(data.type, data.label) < 0)
					array.push({type: data.type, label: data.label, value: data.value, tooltip: data.tooltip});
			}
		},
		
		removeFromSearch: function (elem, data) {
			var type = null;
	  		if ($(elem).hasClass('keyword')) {
	  			type = 'keyword';
	  		} else if ($(elem).hasClass('person')) {
	  			type = 'person';
	  		} else if ($(elem).hasClass('concept')) {
	  			type = 'concept';
	  		} else if ($(elem).hasClass('source')) {
	  			type = 'source';
	  		} else if ($(elem).hasClass('product')) {
	  			type = 'product';
	  		} else
	  			type = 'issue';
			
	  		var label = data.label;
	  		var array = searchTerms[type];
	  		if (type == 'source' || type == 'product' || type == 'person' || type == 'issue') {
	  			var idx = that.indexOfLabel(type, label);
	  			if (idx >= 0)
	  				array.splice(idx);
	  		}
		},
		
		getSearchStr: function (type) {
			var list = searchTerms[type];
			var result = '';
			for (var i = 0; i < list.length; i++) {
				result += list[i].value;
				if (i < list.length - 1)
					result += ',';
			}
			return result;
		},
		
		getTypeV: function (type) {
			return searchTerms[type];
		},
	    
	    init: function () {
	    	searchTerms = {
	    		'person': [],
	    		'source': [],
	    		'product': [],
	    		'issue': []
	    	};
	    }
	};
	
	that.init();
	
	return that;
};


var AlertViz = function(options) { 
    var generalSearch = Search();
    var personSearch = Search();
    
    var itemsPerPage = 100;
    
    var socialGraph = null;
    var chart = null;
    var currentQueryOpts = null;
    
    var normalBarColor = getCssValue('bar-normal', 'background-color');
	var selectedBarColor = getCssValue('bar-selected', 'background-color');
    
    var that = {
    	searchStateGeneral: generalSearch,
    	searchStatePerson: personSearch,
    	
    	cleanData: function () {
    		$('#details_wrapper').html('');
    		$('#wordcloud-div').html('');
    		if (socialGraph != null)
    			socialGraph.clear();
    	},
    	
    	addToSearchField: function (fieldId, data) {
    		generalSearch.addToSearch(data);
    		var selector = '#' + fieldId;
    		
    		if (fieldId == 'keyword_text') {
    			generalSearch.addToSearch(data);

    			var value = data.value;
    			
    			$(selector).val($(selector).val() + ' ' + value);
    		} else {
    			generalSearch.addToSearch(data);
            	
        		var label = data.label;
        		if (data.type == 'source') 
        			$(selector).val(label + ':' + data.type + ':' + data.tooltip + '|');
        		else
        			$(selector).val(label + ':' + data.type + '|');
    		}
    		
    		$(selector).change();
    	},
    	
    	searchRelated: function () {
    		$.ajax({
    			type: 'POST',
    			url: 'query',
    			data: {type: 'suggestMyCode'},
    			dataType: 'json',
    			async: true,
    			success: function (data, textStatus, jqXHR) {
    				that.setQueryResults(data);
    			}
    		});
    		
    		return false;
    	},
    	
    	searchItemDetails: function (itemId) {
    		$.ajax({
    			type: 'POST',
    			url: 'query',
    			data: {type: 'itemDetails', query: itemId},
    			dataType: 'json',
    			async: true,
    			success: function (data, textStatus, jqXHR) {
    				that.setItemDetails(data);
    			}
    		});
    	},
    	
    	searchIssueDetails: function (itemId, itemUri) {
    		$.ajax({
    			type: 'POST',
    			url: 'query',
    			data: {type: 'issueDetails', query: itemId},
    			dataType: 'json',
    			async: true,
    			success: function (data, textStatus, jqXHR) {
    				that.setIssueDetails(data, itemUri);
    			}
    		});
    	},
    	
    	searchCommitDetails: function (itemUri) {
    		$.ajax({
    			type: 'POST',
    			url: 'query',
    			data: {type: 'commitDetails', query: itemUri},
    			success: function (data, textStatus, jqXHR) {
    				that.setCommitDetails(data);
    			}
    		});
    	},
    	
    	setQueryResults: function (data) {
    		if (data.type == 'peopleData') {
        		that.createGraph(data);
        	} else if (data.type == 'timelineData') {
        		that.createTimeline(data);
        	} else if (data.type == 'keywordData') {
        		that.createWordCloud(data.data);
        	} else if (data.type == 'itemData') {
        		that.createItems(data);
        	}
    	},
    	
    	searchQueryGeneral: function (queryType, queryOpts) {
    		$.ajax({
                type: "POST",
                url: "query",
                data: {
                	type: queryType,
                	keywords: queryOpts.keywords,
        			people: queryOpts.people,
        			sources: queryOpts.sources,
        			products: queryOpts.products,
        			issues: queryOpts.issues,
        			from: queryOpts.from,
        			to: queryOpts.to,
        			sort: queryOpts.sort,
        			optional: queryOpts.optional,
        			issuesChk: queryOpts.issuesChk,
        			commitsChk: queryOpts.commitsChk,
        			forumsChk: queryOpts.forumsChk,
        			mailsChk: queryOpts.mailingListsChk,
        			wikisChk: queryOpts.wikisChk,
        			OpenChk: queryOpts.openChk,
        			NoneChk: queryOpts.noneChk,
        			VerifiedChk: queryOpts.verifiedChk,
        			FixedChk: queryOpts.fixedChk,
        			AssignedChk: queryOpts.assignedChk,
        			WondFixChk: queryOpts.wondFixChk,
        			ResolvedChk: queryOpts.resolvedChk,
        			InvalidChk: queryOpts.invalidChk,
        			ClosedChk: queryOpts.closedChk,
        			WorksForMeChk: queryOpts.worksForMeChk,
        			UnknownChk: queryOpts.unknownChk,
        			DuplicateChk: queryOpts.duplicateChk
                },
                dataType: "json",
                async: true,
                success: function (data, textStatus, jqXHR) {
                	that.setQueryResults(data);
                },
                error: function (jqXHR, textStatus, errorThrown) { /* for now do nothing */ }
            });
    	},
    	
    	/*
    	 * Items is special because it contains offset and limit
    	 */
    	searchItemsGeneral: function (queryOpts, offset, limit) {
    		return that.searchItemsByQueryOpts({
    			type: 'itemData',
            	keywords: queryOpts.keywords,
    			people: queryOpts.people,
    			sources: queryOpts.sources,
    			products: queryOpts.products,
    			from: queryOpts.from,
    			to: queryOpts.to,
    			sort: queryOpts.sort,
    			optional: queryOpts.optional,
    			issuesChk: queryOpts.issuesChk,
    			commitsChk: queryOpts.commitsChk,
    			forumsChk: queryOpts.forumsChk,
    			mailsChk: queryOpts.mailingListsChk,
    			wikisChk: queryOpts.wikisChk,
    			OpenChk: queryOpts.openChk,
    			NoneChk: queryOpts.noneChk,
    			VerifiedChk: queryOpts.verifiedChk,
    			FixedChk: queryOpts.fixedChk,
    			AssignedChk: queryOpts.assignedChk,
    			WondFixChk: queryOpts.wondFixChk,
    			ResolvedChk: queryOpts.resolvedChk,
    			InvalidChk: queryOpts.invalidChk,
    			ClosedChk: queryOpts.closedChk,
    			WorksForMeChk: queryOpts.worksForMeChk,
    			UnknownChk: queryOpts.unknownChk,
    			DuplicateChk: queryOpts.duplicateChk,
    			offset: offset,
    			maxCount: limit
    		});
    	},
    	
    	searchItemsByQueryOpts: function (queryOpts) {
    		$.ajax({
                type: "POST",
                url: "query",
                data: queryOpts,
                dataType: "json",
                async: true,
                success: function (data, textStatus, jqXHR) {
                	currentQueryOpts = queryOpts;
                	that.setQueryResults(data);
                },
                error: function (jqXHR, textStatus, errorThrown) { /* for now do nothing */ }
            });
    	},
    	
    	searchKeywordsGeneral: function (queryOpts) {
    		that.searchQueryGeneral('keywordData', queryOpts);
    	},
    	
    	searchTimelineGeneral: function (queryOpts) {
    		that.searchQueryGeneral('timelineData', queryOpts);
    	},
    		
    	searchPeopleGeneral: function (queryOpts) {
    		that.searchQueryGeneral('peopleData', queryOpts);
    	},
    	
    	searchGeneral: function() {	
    		var keywords = $('#keyword_text').val();
    		var people = generalSearch.getSearchStr('person');
    		var sources = generalSearch.getSearchStr('source');
    		var products = generalSearch.getSearchStr('product');
    		var issues = generalSearch.getSearchStr('issue');
    		
    		var from = $('#from_text').val();
    		var to = $('#to_text').val();
    		
    		var queryOpts = {
    			type: 'generalSearch',
    			keywords: keywords,
    			people: people,
    			sources: sources,
    			products: products,
    			issues: issues,
    			from: from,
    			to: to,
    			sort: ($('#relevance_sort').attr('checked') == 'checked') ? 'relevance' : 'dateDesc',
    			optional: $('#use_or_check').attr('checked') == 'checked',
    			issuesChk: $('#issues_check').attr('checked') == 'checked',
    			commitsChk: $('#commits_check').attr('checked') == 'checked',
    			forumsChk: $('#forums_check').attr('checked') == 'checked',
    			mailingListsChk: $('#mailing_check').attr('checked') == 'checked',
    			wikisChk: $('#wikis_check').attr('checked') == 'checked',
    			openChk: $('#gen_open_check').attr('checked') == 'checked',
    			noneChk: $('#gen_none_check').attr('checked') == 'checked',
    			verifiedChk: $('#gen_verified_check').attr('checked') == 'checked',
    			fixedChk: $('#gen_fixed_check').attr('checked') == 'checked',
    			assignedChk: $('#gen_assigned_check').attr('checked') == 'checked',
    			wondFixChk: $('#gen_wont_check').attr('checked') == 'checked',
    			resolvedChk: $('#gen_resolved_check').attr('checked') == 'checked',
    			invalidChk: $('#gen_invalid_check').attr('checked') == 'checked',
    			closedChk: $('#gen_closed_check').attr('checked') == 'checked',
    			worksForMeChk: $('#gen_works_check').attr('checked') == 'checked',
    			unknownChk: $('#gen_unknown_check').attr('checked') == 'checked',
    			duplicateChk: $('#gen_duplicate_check').attr('checked') == 'checked'
    		};
			
			that.searchKeywordsGeneral(queryOpts);
			that.searchTimelineGeneral(queryOpts);
			that.searchItemsGeneral(queryOpts, 0, itemsPerPage);
			that.searchPeopleGeneral(queryOpts);
			
			that.cleanData();
			return false;
    	},
    	
    	searchIssueId: function () {
    		var issues = $('#issue_id_text').val();
    		
    		return that.searchIssueByQueryOpts({
				type: 'duplicateIssue',
    			issues: issues,
    			NoneChk: $('#dup_none_check').attr('checked') == 'checked',
    			FixedChk: $('#dup_fixed_check').attr('checked') == 'checked',
    			WontFixChk: $('#dup_wont_check').attr('checked') == 'checked',
    			InvalidChk: $('#dup_invalid_check').attr('checked') == 'checked',
    			DuplicateChk: $('#dup_duplicate_check').attr('checked') == 'checked',
    			WorksForMeChk: $('#dup_works_check').attr('checked') == 'checked',
    			UnknownChk: $('#dup_unknown_check').attr('checked') == 'checked',
    			OpenChk: $('#dup_open_check').attr('checked') == 'checked',
    			VerifiedChk: $('#dup_veririfed_check').attr('checked') == 'checked',
    			AssignedChk: $('#dup_assigned_check').attr('checked') == 'checked',
    			ResolvedChk: $('#dup_resolved_check').attr('checked') == 'checked',
    			ClosedChk: $('#dup_closed_check').attr('checked') == 'checked',
            	offset: 0,
            	limit: itemsPerPage
    		});
    	},
    	
    	searchIssueByQueryOpts: function (queryOpts) {
    		try {
	    		$.ajax({
	                type: "GET",
	                url: "query",
	                data: queryOpts,
	                dataType: "json",
	                async: true,
	                success: function (data, textStatus, jqXHR) {
	                	currentQueryOpts = queryOpts;
	    				that.setQueryResults(data);
	    			},
	                error: function (jqXHR, textStatus, errorThrown) { /* for now do nothing */ }
	            });
    		} catch (e) {
    			alert(e);
    		}
    		that.cleanData();
    		return false;
    	},
    	
    	searchPeople: function () {
    		var people = personSearch.getSearchStr('person');
    		
    		$.ajax({
    			type: "GET",
                url: "query",
                dataType: "xml",
                async: true,
                data: {
                	type: 'suggestPeople',
                	people: people
                },
                success: function (xml, textStatus, jqXHR) {
                	// TODO not implemented
                }
    		});
    		
    		return false;
    	},
    	
    	jumpPage: function (offset, limit) {
    		var queryOpts = currentQueryOpts;
    		queryOpts.offset = offset;
    		queryOpts.limit = limit;
    		
    		switch(queryOpts.type) {
    		case 'duplicateIssue':
    			return that.searchIssueByQueryOpts(queryOpts);
    		case 'itemData':
    			return that.searchItemsByQueryOpts(queryOpts);
    		}
    	},
    	
    	setIssueDetails: function (data, selectedUri) {
    		// generate accordion
    		// item description
    		var html = '<div class="details_section">';
    		html += '<table class="heading"><tr>';
    		html += '<td class="title_desc">Issue created by ';
    		html += '<span class="headings_author">' + (data.author == null ? 'Unknown' : data.author.name) + '</span>';
    		html += '<span class="headings_date">' + (data.dateOpened == null ? '' : new Date(data.dateOpened).format(defaultDateFormat)) + '</span>';
    		html += '<br /><span class="headings_status">' + data.resolution + ', ' + data.status + '</span>';
    		html += '</td>';
    		html += '</tr></table>';
    		// content
    		html += '<div class="content' + (data.url == selectedUri ? ' selected_issue' : '') + '"><table id="item_details"><tr><td colspan="3"><div id="item-accordion">' + data.description + '</div></td></tr></table></div>';
    		html += '</div>';
    		
    		// comments
    		var comments = data.comments;
    		for (var i = 0; i < comments.length; i++) {
    			var comment = comments[i];
    			
    			html += '<div class="details_section">';
    			html += '<table class="heading"><tr>';
    			html += '<td class="title_comm">Comment by <span class="headings_author">' + comment.person.name + '</span><span class="headings_date">' + new Date(comment.commentDate).format(defaultDateFormat) + '</span></td>';
    			html += '</tr></table>';
    			// content
    			
    			if (comment.commentUri == selectedUri)
    				html += '<div class="content selected_issue">' + comment.commentText + '</div>';
    			else
    				html += '<div class="content">' + comment.commentText + '</div>';
    			html += '</div>';
    		}
    		
    		// related issues TODO

    		// insert html
    		$('#details_wrapper').html(html);
    		jQuery(".content").hide();
    		jQuery(".selected_issue").show();
    		jQuery(".heading").click(function() {
			    jQuery(this).next(".content").slideToggle(500);
			});
    	},
    	
    	addCommitToSearch: function (name, uri, tooltip) {
			var event = window.event;
			event.stopPropagation();
			event.preventDefault();
			
			viz.addToSearchField('other_text', {type: 'source', label: name, value: uri, tooltip: tooltip});
			
			return false;
		},
    	
    	setCommitDetails: function (data) {
    		// header
    		var html = '<div class="details_section">';
    		html += '<table class="heading"><tr>';
    		html += '<td class="title_desc">Commit comment</td>';
    		
    		html += '<td>Committer: <div id="author_desc" class="data">' + data.committer.name + '</div></td>';
    		if (data.author != null) html += '<td>Author: <div id="author_desc" class="data">' + data.author.name + '</div></td>';
    		html += '<td>Date: <div id="date_desc" class="data">' + new Date(data.commitDate).format(defaultDateFormat) + '</div></td>';
    		html += '<td>Revision: <div id="resolution_desc" class="data">' + data.revisionTag + '</div></td>';
    		
    		html += '</tr></table>';
    	
    		// content
    		html += '<div class="content"><table id="item_details"><tr><td colspan="3"><div id="item-accordion">' + data.message + '</div></td></tr></table></div>';
    		html += '</div>';
    		
    		// files
    		html += '<div class="details_section">';
    		html += '<table class="heading"><tr>';
    		html += '<td class="title_comm">Files</td>';
    		html += '</tr></table>';
    		
    		html += '<div class="content" id="files_content">';
    		html += '<ul class="tree_ul">';
    		
    		// create file tree
    		var files = data.files;
    		for (var fileIdx = 0; fileIdx < files.length; fileIdx++) {
    			var file = files[fileIdx];
    			var modules = file.modules;
    			
    			if (modules.length > 0) {
    				// create a tree of modules
    				html += '<li class="tree_li">';
    				html += '<div class="toggle" title="' + file.fullName + '">';
    				html += '<span>' + file.name + ' (' + file.action + ')</span>';
    				html += '<img src="img/search-16.png" alt="Search" onclick="return viz.addCommitToSearch(\'' + file.name + '\',\'' + file.uri + '\',\'' + file.fullName + '\');" />';
    				html += '</div>';
    				html += '<ul class="tree_ul">';
    				for (var moduleIdx = 0; moduleIdx < modules.length; moduleIdx++) {
    					var module = modules[moduleIdx];
    					var methods = module.methods;
    					
    					if (methods.length > 0) {
    						// create methods
        					html += '<li class="tree_li">';
        					html += '<div class="toggle">';
        					html += '<span>' + module.name + ' (' + module.startLine + '-' + module.endLine + ')</span>';
        					html += '<img src="img/search-16.png" alt="Search" onclick="return viz.addCommitToSearch(\'' + module.name + '\',\'' + module.uri + '\',\'' + file.fullName + '\');" />';
        					html +='</div>';
    	    				html += '<ul class="tree_ul">';
    	    				for (var methodIdx = 0; methodIdx < methods.length; methodIdx++) {
    	    					var method = methods[methodIdx];
    	    					html += '<li class="tree_li">';
    	    					html += '<span class="leaf">' + method.methodName + ' (' + method.startLine + '-' + method.endLine + ')</span>';
    	    					html += '<img src="img/search-16.png" alt="Search" onclick="return viz.addCommitToSearch(\'' + method.methodName + '\',\'' + method.methodUri + '\',\'' + file.fullName + '\');" />';
    	    					html += '</li>';
    	    				}
    	    				html += '</ul>';
    	    				html += '</li>';
    					} else {
        					html += '<li class="tree_li">';
        					html += '<span class="leaf">' + module.name + ' (' + module.startLine + '-' + module.endLine + ')</span>';
        					html += '<img src="img/search-16.png" alt="Search" onclick="return viz.addCommitToSearch(\'' + module.name + '\',\'' + module.uri + '\',\'' + file.fullName + '\');" />';
        					html += '</li>';
    					}
    				}
    				html += '</ul>';
    				html += '</li>';
    			} else {	// create leaf
    				html += '<li class="tree_li">';
    				html += '<span class="leaf">' + file.name + ' (' + file.action + ')</span>';
    				html += '<img src="img/search-16.png" alt="Search" onclick="return viz.addCommitToSearch(\'' + file.name + '\',\'' + file.uri + '\',\'' + file.fullName + '\');" />';
    				html += '</li>';
    			}
    		}
    		
    		html += '</ul>';
    		html += '</div>';
    		html += '</div>';
    		
    		
    		$('#details_wrapper').html(html);
    		$('.tree_ul').tree({focusSize: '100%'});
    		jQuery('.content[id != "files_content"]').hide();
    		jQuery('.heading').click(function() {
			    jQuery(this).next('.content').slideToggle(500);
			});
    	},
    	
    	setItemDetails: function (data) {
    		var html = '<table class="mail_content">';
    		
    		// from/to
    		if (data.to != null)
    			html += '<tr class="heading"><td class="author_td">' + data.from.name + ' to ' + data.to.name + '</td><td class="date_td">' + new Date(data.time).format(defaultDateFormat) + '</td></tr>';
    		else
    			html += '<tr class="heading"><td class="author_td">' + data.from.name + '</td><td class="date_td">' + new Date(data.time).format(defaultDateFormat) + '</td></tr>';
    		
    		// subject
    		if (data.subject != null)
    			html += '<tr><td class="subject_td" colspan="2">' + data.subject + '</td></tr>';
    		if (data.content != null)
    			html += '<tr><td class="content_td" colspan="2">' + data.content + '</td></tr>';
    		
    		
    		html += '</table>';
    		$('#details_wrapper').html(html);
    	},
    	
    	createWordCloud: function(data) {
    		var width = $('#wordcloud-div').width();
    		var height = $('#wordcloud-div').height();
    		
    		// add handlers
    		for (var i = 0; i < data.length; i++) {
    			data[i].handlers = {
    					click: function (event) {
    						viz.addToSearchField('keyword_text', {type: 'keyword', value: $(event.srcElement).text()});
    					}
    			};
    		}
    		
    		$('#wordcloud-div').html('');
    		$('#wordcloud-div').jQCloud(data, {width: width, height: height});
    	},
    	
    	createTimeline: function(data) {
    		var nBars = 31;
    		
    		var allDays = data.days;
    		
    		var history = ZoomHistory();
    		var selectedRange = [Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY];
    		var textColor = getCssValue('barchart-axis-title', 'color');
    		var yLabelsColor = getCssValue('barchart-yaxis-labels', 'color');
    		var xLabelsColor = getCssValue('barchart-xaxis-labels', 'color');
    		var ctrlDown = false;
    		$(document).keydown(function (event) {
    			if (event.keyCode == 17)	// CTRL
    				ctrlDown = true;
    		});
    		$(document).keyup(function (event) {
    			if (event.keyCode == 17)	// CTRL
    				ctrlDown = false;
    		});
    		
    		function createSeries(nBars, days) {
    			if (days.length == 0 || days.length == 1)
    				return [];
    			
    			var startDate = days[0][0];
    			var endDate = days[days.length - 1][0];
    			
    			var nDays = days.length;
    			var perBar = Math.ceil(nDays/nBars);
    			var barTimeInterval = (endDate - startDate) / nBars;
    			
    			var result = [];
    			var dayIdxGlob = 0;
    			var currentTime = startDate + barTimeInterval/2;
    			for (var barIdx = 0; barIdx < nBars; barIdx++) {
    				var nPosts = 0;
    				
    				for (var i = 0; i < perBar && dayIdxGlob < nDays; i++) {
    					nPosts += days[dayIdxGlob++][1];
    				}
    				
    				result.push({x: currentTime, y: nPosts, color: (currentTime >= selectedRange[0] && currentTime <= selectedRange[1]) ? selectedBarColor : normalBarColor});
    				currentTime += barTimeInterval;
    			}
    			
    			return result;
    		}
    		
    		var seriesV = createSeries(nBars, allDays);
    		
    		function getPointColor(year, month) {
    	    	var fromDate = $('#from_text').datepicker('getDate');
    	    	var toDate = $('#to_text').datepicker('getDate');
    	    	
    	    	if (fromDate == null || toDate == null)
    	    		return normalBarColor;
    	    	
    	    	var color = selectedBarColor;
    			if (fromDate == null || toDate == null)
    				color = normalBarColor;
    			else if (year < fromDate.getFullYear() || (year == fromDate.getFullYear() && month < fromDate.getMonth() + 1))
    				color = normalBarColor;
    			else if (year > toDate.getFullYear() || (year == toDate.getFullYear() && month > toDate.getMonth() + 1))
    				color = normalBarColor;
    			
    			return color;
    	    }
    		
			$('#inner-south').html('<div id="chart-div" ></div>');
			
			chart = new Highcharts.Chart({
				chart: {
					renderTo: 'chart-div',
					height: 195,
					defaultSeriesType: 'column',
					margin: [ 5, 10, 50, 50],
					zoomType: 'x',
					ignoreHiddenSeries: true,
					events: {
						selection: function (event) {
							if (ctrlDown) {	// if CTRL+zoom => select columns
								event.preventDefault();
								
								var min = event.xAxis[0].min;
								var max = event.xAxis[0].max;
								
								selectedRange = [min, max];
								
								$('#from_text').datepicker("setDate", new Date(min));
								$('#to_text').datepicker("setDate", new Date(max));
								
								$('#from_text').change();
								$('#to_text').change();
							}
							
							return true;
						}
					}
				},
				colors: [normalBarColor],
				credits: {enabled: false},
				title: null,
				toolbar: {
			        itemStyle: {
			            color: textColor
			        }
			    },
				xAxis: {
					type: 'datetime',
					minRange: 1000*3600*24*31,	// == 1month
					labels: {
						style: {
							color: xLabelsColor
						}
					},
					events: {
						setExtremes: function (event) {
							var min = event.min;
							var max = event.max;
							
							if (min != null && max != null && max - min < this.options.minRange)
								return false;
							
							// update the zooming history
							if (min == null || max == null)
								history.clear();
							else
								history.addItem(min, max);
							
							// process the event
							if (min == null || max == null)
								chart.series[0].setData(createSeries(nBars, allDays));
							else {
								// filter the days which will be in the timeline
								var newDays = [];
								jQuery.each(allDays, function (i, point) {
									if (point[0] >= min && point[0] < max)
										newDays.push(point);
								});
								
								var newSeriesV = createSeries(nBars, newDays);
								chart.series[0].setData(newSeriesV);
							}
							return true;
						}
					}
				},
				yAxis: {
					min: 0,
					title: {
						text: 'Posts',
						style: {
							color: textColor
						}
					},
					labels: {
						style: {
							color: yLabelsColor
						}
					}
				},
				legend: {
					enabled: false
				},
				tooltip: {
					formatter: function() {
						return 'Number of posts: ' + Highcharts.numberFormat(this.y, 0);
							 
					}
				},
				plotOptions: {
					column: {
						cursor: 'pointer'
					}
				},
				series: [
					{
						name: 'Posts',
						data: seriesV
					}
				]
			});
			
			// read some forums of Highcharts, it appears this is the only way to 
			// register a right-click listener
			$('#chart-div').bind('mouseup', function (event) {
				if (event.button == 2) {
					var item = history.getPrevious();
					history.getPrevious();	// need to pop 2, because I add the same item back in the listener
					
					if (item != null) {
						chart.xAxis[0].setExtremes(item.min, item.max);
						
						if (item.min == null && item.max == null) {
							try {
								chart.toolbar.remove('zoom');
							} catch (e) {}
						} else {
							chart.toolbar.add('zoom', 'Reset zoom', 'Reset zoom', function () {
								chart.xAxis[0].setExtremes(null, null);
								try {
									chart.toolbar.remove('zoom');
								} catch (e) {}
							});
						}
					}
				}
				return true;
			});
			$('#chart-div').bind('contextmenu', function () {
				return false;
			});
    	},
    	
    	createItems: function(data) {
    		var Type = {"email": 10, "post": 11, "bugDescription": 12, "bugComment": 13, "commit": 14, "wikiPost": 15};
    		
    		$('#items-div').html('');
    		var html = '<ul>';
			
			var peopleH = data.persons;
			var items = data.items;
    		
			// generate HTML
			for(var i = 0; i < data.items.length; i++){
    			var item = items[i];
    			html += '<li>';
    			
    			switch (item.type) {
    			case Type.email:
    				html += '<div class="item-wrapper email" onclick="viz.searchItemDetails(' + item.id + ')"><table class="item_table">';
    				
    				// sender receiver
    				html += '<tr>';
    				var toNameList = [];
    				var toIdList = item.recipientIDs;
    				for (var j = 0; j < toIdList.length; j++)
    					toNameList.push(peopleH[toIdList[j]].name);
    				
    				if (toNameList.length > 0)
    					html += '<td class="item_header">' + peopleH[item.senderID].name + ' to ' + toNameList.join(', ') + '</td>';
    				else
    					html += '<td class="item_header">' + peopleH[item.senderID].name + '</td>';
    				
    				// date
					html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
					
					// subject
					html += '<tr>';
					if (item.url != null)
						html += '<td colspan="2" class="item_subject" colspan="2"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
					else
						html += '<td colspan="2" class="item_subject" colspan="2">' + item.subject + '</td>';
					html += '</tr>';
    				
    				// content
					html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
	    			html += '</table></div>';
    				break;
    			case Type.post:
    				html += '<div class="item-wrapper email" onclick="viz.searchItemDetails(' + item.id + ')"><table class="item_table">';
    				
    				// author + date
    				html += '<tr>';
    				html += '<td class="item_header">' + peopleH[item.senderID].name + '</td>';
    				html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
					
					html += '<tr>';
					if (item.url != null)
						html += '<td colspan="2" class="item_subject" colspan="2"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
					else
						html += '<td colspan="2 class="item_subject" colspan="2">' + item.subject + '</td>';
					html += '</tr>';
    				
					// content
					html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
	    			html += '</table></div>';
    				break;
    			case Type.bugDescription:
    				html += '<div class="item-wrapper issue" onclick="viz.searchIssueDetails(' + item.issueID + ',\'' + item.entryID + '\')"><table class="item_table">';
    				
    				// from + date
					html += '<tr>';
					html += '<td class="item_header">' + (peopleH[item.authorID] != null ? peopleH[item.authorID].name : 'unknown') + (item.similarity != null ? ' <span class="item_similarity">' + (item.similarity*100).toFixed(0) + '% sim.</span>' : '') + '</td>';
					html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
	    			
					// subject + similarity
					html += '<tr>';
					if (item.url != null)
						html += '<td colspan="2" class="item_subject"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
					else
						html += '<td colspan="2" class="item_subject">' + item.subject + '</td>';
					
					html += '</tr>';
					
					// content
					html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
	    			html += '</table></div>';
    				break;
    			case Type.bugComment:
    				html += '<div class="item-wrapper comment" onclick="viz.searchIssueDetails(' + item.issueID + ',\'' + item.entryID + '\')"><table class="item_table">';
    				
    				// from + date
    				html += '<tr>';
					html += '<td class="item_header">' + peopleH[item.senderID].name + (item.similarity != null ? ' <span class="item_similarity">' + (item.similarity*100).toFixed(0) + '% sim.</span>' : '') + '</td>';
					html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
					
					// subject + similarity
					html += '<tr>';
					if (item.url != null)
						html += '<td colspan="2" class="item_subject"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
					else
						html += '<td colspan="2" class="item_subject">' + item.subject + '</td>';
					html += '</tr>';
    				
					// content
					html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
	    			html += '</table></div>';
    				break;
    			case Type.commit:
    				html += '<div class="item-wrapper commit" onclick="viz.searchCommitDetails(\'' + item.entryID + '\')"><table class="item_table">';
    				
    				// author + date
    				html += '<tr>';
					html += '<td class="item_header">' + peopleH[item.authorID].name + '</td>';
					html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
    				
					// content
    				html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
        			html += '</table></div>';
    				break;
    			case Type.wikiPost:
    				html += '<div class="item-wrapper comment" onclick="viz.searchItemDetails(' + item.id + ')"><table class="item_table">';
    				
    				// author + date
    				html += '<tr>';
					html += '<td class="item_header">' + peopleH[item.senderID].name + '</td>';
					html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
    				
					// content
    				html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
        			html += '</table></div>';
    				break;
    			}
				
    			html += '</li>';
    		}
    		html += '</ul>';
    		
    		// navigation
    		var info = data.info;
    		var offset = info.offset;
    		var total = info.totalCount == null ? Number.POSITIVE_INFINITY : info.totalCount;
    		
    		var nPages = Math.ceil(total/itemsPerPage);
    		var currentPage = Math.floor(offset/itemsPerPage) + 1;
    		
    		var navHtml = (nPages == Number.POSITIVE_INFINITY) ? 'page ' + currentPage : 'page ' + currentPage + ' of ' + nPages;	// TODO
    		if (currentPage > 1)
    			navHtml = '<a onclick="viz.jumpPage(' + (info.offset - info.limit) + ', ' + info.limit + ')">&lt;&lt;</a> ' + navHtml;
    		if (currentPage < nPages)
    			navHtml += ' <a onclick="viz.jumpPage(' + (info.offset + info.limit) + ', ' + info.limit + ')">&gt;&gt;</a>';
    		
    		$('#items-div').html(html);
    		$('#page_td').html(navHtml);
    		
    		// set class selected when clicking an item
    		$('.item-wrapper').click(function (event) {
    			// first remove the selected class from all the items
    			$('.item-selected').removeClass('item-selected');
    			// then add selected class to the item clicked
    			$(event.currentTarget).addClass('item-selected');
    		});
    	},

    	createGraph: function(data) {
    		if (socialGraph == null) {
	    		socialGraph = SocialGraph({
	    			step: 5,
	    			container: 'graph-div',    			
	    		});
    		}
    		socialGraph.init(data);
    	},
    	
    	decreaseGraph: function() {
    		if(socialGraph)
    			socialGraph.showLess();
    	},
    	
    	increaseGraph: function() {
    		if(socialGraph)
    			socialGraph.showMore();
    	}
    };
    
    
    // init the search text fields
    $('#keyword_text').change(function (event) {
    	updateUrl();
    });
    
    $('#other_text').autoSuggest('suggest', {
    	selectedItemProp: 'label',
    	searchObjProps: 'label',
    	selectedValuesProp: 'value',
    	queryParam: 'Other',
    	retrieveLimit: false,
    	neverSubmit: true,
    	startText: 'people, products, sources, components, issue IDs,...',
    	asHtmlID: 'other_text',
    	addByWrite: false,
    	selectionAdded: function(elem, data) {
    		if (data.type == 'source')
				$(elem).attr('title', data.tooltip);
    		
    		if (!settingManually) {
	    		generalSearch.addToSearch(data);
	    		updateUrl();
    		}
    	},
	  	selectionRemoved: function(elem, data) {
	  		generalSearch.removeFromSearch(elem, data);
	  		updateUrl();
	  		
	  		elem.remove();
	  	},
	  	formatList: function (data, el) {
	  		if (data.type == 'source') {
	  			$(el).attr('title', data.tooltip);
	  			$(el).html($(el).html() + ' <span class="file_path">(' + data.path + ')</span>');
	  		}
	  		return el;
	  	}
    });
    
    function setRange() {
    	var fromDate = $('#from_text').datepicker('getDate');
    	var toDate = $('#to_text').datepicker('getDate');
    	
    	if (chart != null) {
    		var min = fromDate == null ? Number.POSITIVE_INFINITY : fromDate.getTime();
    		var max = toDate == null ? Number.NEGATIVE_INFINITY : toDate.getTime();
    		
    		$.each(chart.series[0].data, function (idx, point) {
				point.update({color: (point.x >= min && point.x <= max) ? selectedBarColor : normalBarColor}, false);
			});
    	}
    }
    
    $('#from_text').change(function (event) {
    	setRange();
    	updateUrl();
    });
	$('#to_text').change(function (event) {
		setRange();
		updateUrl();
	});
    
    // issue id search
    $('#issue_id_text').autocomplete({
    	source: function (request, response) {
    		$.ajax({
				url: "suggest",
				dataType: "json",
				data: {
					Issues: request.term
				},
				success: function(data) {
					response(data);
				}
			});
    	},
    	response: function (event, ui) {
    		alert('works');
    	}
    }).data('autocomplete')._renderItem = function (ul, item) {
    	var term = this.term.split(' ').join('|');
		var re = new RegExp("(" + term + ")", "gi") ;
		var t = item.label.replace(re,"<em>$1</em>");
		return $( "<li></li>" )
		.data( "item.autocomplete", item )
		.append( "<a>" + t + "</a>" )
		.appendTo( ul );
    };
    
    $('#issue_id_text').blur(function (event) {
    	updateUrl();
    });
    
    // suggest people search
    $('#person_text').autoSuggest('suggest', {
    	selectedItemProp: 'label',
    	searchObjProps: 'label',
    	selectedValuesProp: 'value',
    	queryParam: 'People',
    	retrieveLimit: false,
    	neverSubmit: true,
    	startText: 'people,...',
    	asHtmlID: 'person_text',
    	addByWrite: false,
    	selectionAdded: function(elem, data) {
    		if (!settingManually) {
	    		personSearch.addToSearch(data);
	    		updateUrl();
    		}
    	},
	  	selectionRemoved: function(elem, data) {
	  		personSearch.removeFromSearch(elem, data);
	  		updateUrl();
	  		elem.remove();
	  	}
    });
    
    // tab handler
    jQuery.each($('#navigation').find('a'), function (i, a) {
    	$(a).click(function () {
    		// when a tab is clicked, the URL has to be updated
    		currentTab = i;
    		updateUrl();
    	});
    });
    
    // buttons on social graph
    $('#btnShowMore').click(function(){
		viz.increaseGraph();
	});			
	$('#btnShowLess').click(function(){
		viz.decreaseGraph();
	});
	
	// datepickers
	$('#from_text').datepicker({dateFormat: 'yy-mm-dd', constrainInput: true});
	$('#to_text').datepicker({dateFormat: 'yy-mm-dd', constrainInput: true});
    
    // make the items div and item details div resizable
	var leftItemWidth = $('.item_left').width();
	var rightItemWidth = $('.item_right').width();
    $('#separator').draggable({
		axis: 'x',
		grid: [20, 20],
		containment: [$('#separator').offset().left - 100, 0, $('#separator').offset().left + 200, 10000],
		drag: function (event, ui) {
			event.stopPropagation();
			var prevWidth = $('.item_right').width();
			
			$('.item_left').width(leftItemWidth + ui.position.left);
			$('.item_right').width(rightItemWidth - ui.position.left);
			
			// resize the social graph and wordcloud
			var width = $('.item_right').width();
	    	
			if (width != prevWidth) {
		    	// social graph
		    	if (socialGraph != null) socialGraph.resize(width, socialGraph.getHeight());
		    	
		    	// wordcloud
		    	var prevCenter = prevWidth/2;
		    	var newCenter = width/2;
		    	
		    	var dx = newCenter - prevCenter;
		    	$.each($('#wordcloud-div').children('span'), function (idx, el) {
		    		var left = $(el).position().left;
		    		$(el).css('left', (left + dx) + 'px');
		    	});
			}
		},
		start: function (event, ui) {
			leftItemWidth = $('.item_left').width();
			rightItemWidth = $('.item_right').width();
		}
	});
    
    // search with enter
    $('#step0, #step1, #step2, #step3').keydown(function (event) {
    	if (event.keyCode == 13) {
	    	event.stopPropagation();
	    	event.preventDefault();
	    	
	    	var fieldId = $(event.currentTarget).attr('id');
	    	switch (fieldId) {
	    	case 'step0':
	    		that.searchGeneral();
	    		break;
	    	case 'step1':
	    		that.searchIssueId();
	    		break;
	    	case 'step2':
	    		// TODO implement
	    		break;
	    	case 'step3':
	    		// TODO implement
	    		break;
	    	}
	    	
	    	return false;
    	}
    });
    
    return that;
};
