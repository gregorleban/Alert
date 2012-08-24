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
	
	if (state != null)
		result = window.location.pathname.indexOf('index.html') < 0 ? window.location.pathname + 'index.html?' + $.param(state) : window.location.pathname + '?' + $.param(state);
	else
		result = window.location.pathname.replace('index.html', '');
	
	if (currentTab != 0)
		result += result.indexOf('index.html') < 0 ? 'index.html#' + currentTab : '#' + currentTab;
	
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
	var keywords = searchGeneral.getTypeV('keyword');
	var concepts = searchGeneral.getTypeV('concept');
	var sources = searchGeneral.getTypeV('source');
	var products = searchGeneral.getTypeV('product');
	var issues = searchGeneral.getTypeV('issue');
	
	
	
	if (people.length > 0) general.people = people;
	if (keywords.length > 0) general.keywords = keywords;
	if (concepts.length > 0) general.concepts = concepts;
	if (sources.length > 0) general.sources = sources;
	if (products.length > 0) general.products = products;
	if (issues.length > 0) general.issues = issues;
	
	// checkboxes
	var issueChk = $('#issues_check').attr('checked') == 'checked';
	var commitsChk = $('#commits_check').attr('checked') == 'checked';
	var forumsChk = $('#forums_check').attr('checked') == 'checked';
	var mailingChk = $('#mailing_check').attr('checked') == 'checked';
	var wikisChk = $('#wikis_check').attr('checked') == 'checked';
	
	if (!issueChk) general.is = false;
	if (!commitsChk) general.c = false;
	if (!forumsChk) general.fi = false;
	if (!mailingChk) general.m = false;
	if (!wikisChk) general.wo = false;
	
	// dates
	var fromDate = $('#from_text').val();
	var toDate = $('#to_text').val();
	
	if (fromDate != null && fromDate != '') general.from = fromDate;
	if (toDate != null && toDate != '') general.to = toDate;
	
	// duplicate issue
	var issueId = $('#issue_id_text').val();
	if (issueId.length > 0) duplicate.iid = issueId;
	
	var duplUnconfirmedChk = $('#dup_unconfirmed_check').attr('checked') == 'checked';
	var duplNewChk = $('#dup_new_check').attr('checked') == 'checked';
	var duplAssignedChk = $('#dup_assigned_check').attr('checked') == 'checked';
	var duplResolvedChk = $('#dup_resolved_check').attr('checked') == 'checked';
	var duplInvalidChk = $('#dup_invalid_check').attr('checked') == 'checked';
	var duplWorksChk = $('#dup_works_check').attr('checked') == 'checked';
	var duplFixedChk = $('#dup_fixed_check').attr('checked') == 'checked';
	var duplWondChk = $('#dup_wond_check').attr('checked') == 'checked';
	var duplDuplicateChk = $('#dup_duplicate_check').attr('checked') == 'checked';
	
	if (!duplUnconfirmedChk) duplicate.u = false;
	if (!duplNewChk) duplicate.n = false;
	if (!duplAssignedChk) duplicate.a = false;
	if (!duplResolvedChk) duplicate.r = false;
	if (!duplInvalidChk) duplicate.i = false;
	if (!duplWorksChk) duplicate.w = false;
	if (!duplFixedChk) duplicate.f = false;
	if (!duplWondChk) duplicate.wnd = false;
	if (!duplDuplicateChk) duplicate.d = false;
	
	// issues related to my code
	var myUnconfirmedChk = $('#my_unconfirmed_check').attr('checked') == 'checked';
	var myNewChk = $('#my_new_check').attr('checked') == 'checked';
	var myAssignedChk = $('#my_assigned_check').attr('checked') == 'checked';
	var myResolvedChk = $('#my_resolved_check').attr('checked') == 'checked';
	var myInvalidChk = $('#my_invalid_check').attr('checked') == 'checked';
	var myWorksChk = $('#my_works_check').attr('checked') == 'checked';
	var myFixedChk = $('#my_fixed_check').attr('checked') == 'checked';
	var myWondChk = $('#my_wond_check').attr('checked') == 'checked';
	var myDuplicateChk = $('#my_duplicate_check').attr('checked') == 'checked';
	
	if (!myUnconfirmedChk) myCode.u = false;
	if (!myNewChk) myCode.n = false;
	if (!myAssignedChk) myCode.a = false;
	if (!myResolvedChk) myCode.r = false;
	if (!myInvalidChk) myCode.i = false;
	if (!myWorksChk) myCode.w = false;
	if (!myFixedChk) myCode.f = false;
	if (!myWondChk) myCode.wnd = false;
	if (!myDuplicateChk) myCode.d = false;
	
	// construct result
	if (Object.getOwnPropertyNames(general).length > 0) result.gen = general;
	if (Object.getOwnPropertyNames(duplicate).length > 0) result.dupl = duplicate;
	if (Object.getOwnPropertyNames(myCode).length > 0) result.mc = myCode;

	return Object.getOwnPropertyNames(result).length === 0 ? null : result;
}

function updateUrl() {
	if (!settingManually) {
		var url = genCurrentUrl();
		var state = getCurrentState();
		
		history.replaceState(state, '', url);
	}
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
	case 'u':
		selector += 'unconfirmed_check';
		break;
	case 'n':
		selector += 'new_check';
		break;
	case 'a':
		selector += 'assigned_check';
		break;
	case 'r':
		selector += 'resolved_check';
		break;
	case 'i':
		selector += 'invalid_check';
		break;
	case 'w':
		selector += 'works_check';
		break;
	case 'f':
		selector += 'fixed_check';
		break;
	case 'wnd':
		selector += 'wond_check';
		break;
	case 'd':
		selector += 'duplicate_check';
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
	var searchTerms = {people: true, keywords: true, concepts: true, sources: true, products: true, issues: true};
	var filterChks = {is: true, c: true, fi: true, m: true, wo: true};
	var issueChks = {u: true, n: true, a: true, r: true, i: true, w: true, f: true, wnd: true, d: true};
	
	var dates = {from: true, to: true};
	
	if (general != null) {
		// go through all the properties
		for (var attribute in general) {
			var value = general[attribute];
			
			if (searchTerms[attribute]) {	// search terms
				// the value is an array of search terms
				for (var i = 0; i < value.length; i++) {
					var fieldId = attribute == 'keywords' ? 'keyword_text' : 'other_text';
					viz.addToSearchField(fieldId, value[i]);
				}
			} else if(filterChks[attribute]) {	// checkboxes
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
				}
				
				$(selector).attr('checked', false);
			} else if (dates[attribute]) {
				var field = attribute == 'from' ? 'from_text' : 'to_text';
				$('#' + field).val(value);
			}
		}
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
	var width = $('#graph-div').width();
	var height = $('#graph-div').height();
	
	//read CSS attributes
	var selectedTextClr = getCssValue("selected-node", "color");
	var selectedBoxClr = getCssValue("selected-node", "background-color");
	var neighbourTextClr = getCssValue("neighbour-node", "color");
	var neighbourBoxClr = getCssValue("neighbour-node", "background-color");
	
	if (selectedTextClr == null) selectedTextClr = "yellow";
	if (selectedBoxClr == null) selectedBoxClr = "rgba(30, 116, 255, .6)";
	if (neighbourTextClr == null) neighbourTextClr = "white";
	if (neighbourBoxClr == null) selectedBoxClr = "rgba(62, 189, 255, .6)";
	
	var that = {
		selectedTextClr: selectedTextClr,
		selectedBoxClr: selectedBoxClr,
		neighbourTextClr: neighbourTextClr,
		neighbourBoxClr: neighbourBoxClr,
		
		step: options.step,
		minDisplayLevel: 3,
		
		data: null,
		currentDisplayLevel: null,
			
		graph: DynamicGraph({
			container: 'graph-div',
			width: width,
			height: height,
			draggable: true,
			selectionMode: 'single',
			
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
				'dblclick': function (event, node) {
					event.cancelBubble = true;
					viz.addToSearchField('other_text', {type: 'person', label: node.data.label, value: node.data.email});
					node.select(true);
				},
				'mouseover': function (event, node) {
					event.cancelBubble = true;
					document.body.style.cursor = 'pointer';
				},
				'mouseout': function (event, node) {
					event.cancelBubble = true;
					document.body.style.cursor = 'default';
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
		}
	};
	
	return that;
};

var ZoomHistory = function () {
	var that = {
		current: {min: null, max: null},
		items: [],
		
		addItem: function (min, max) {
			// before it was if (current != null), but current is never null
			that.items.push(that.current);
			that.current = {min: min, max: max};
		},
		
		getPrevious: function () {
			if (that.items.length > 0) {
				that.current = that.items.pop();
				return that.current;
			} return null;
		},
		
		clear: function () {
			that.items = [];
			that.current = {min: null, max: null};
		},
		
		isEmpty: function () {
			return that.items.length == 0;
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
			if (data.type == 'concept' || data.type == 'source' || data.type == 'product' || data.type == 'person') {
				// have to send URI
				var value = data.value;
				var label = data.label;
				
				var array = searchTerms[data.type];
				if (that.indexOfLabel(data.type, label) < 0)
					array.push({type: data.type, label: label, value: value});
			} else if (that.indexOfLabel(data.type, data.label) < 0)
				searchTerms[data.type].push({type: 'keyword', label: data.label, value: data.label});
		},
		
		removeFromSearch: function (elem) {
			// get the type
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
	  		
	  		var array = searchTerms[type];
	  		if (type == 'concept' || type == 'source' || type == 'product' || type == 'person') {
	  			var idx = that.indexOfLabel(type, $(elem).text().substring(1));
	  			if (idx >= 0)
	  				array.splice(idx);
	  		} else {
		  		var idx = array.indexOf($(elem).text().substring(1));
		  		array.splice(idx);
	  		}

	  		elem.remove();
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
	    		'keyword': [],
	    		'person': [],
	    		'concept': [],
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
    
    var that = {
    	searchStateGeneral: generalSearch,
    	searchStatePerson: personSearch,
    	currentQueryOpts: null,
    	socialGraph: null,
    	
    	cleanData: function () {
    		$('#details_wrapper').html('');
    		$('#wordcloud-div').html('');
    		if (that.socialGraph != null)
    			that.socialGraph.clear();
    	},
    	
    	addToSearchField: function (fieldId, data) {
    		generalSearch.addToSearch(data);
        	
    		var label = data.label;
        	var selector = '#' + fieldId;
        	$(selector).val(label + ':' + data.type + '|');
        	$(selector).change();
    	},
    	
    	searchItemDetails: function (itemId) {
    		$.ajax({
    			type: 'GET',
    			url: 'query',
    			data: {type: 'itemDetails', query: itemId},
    			dataType: 'json',
    			async: true,
    			success: function (data, textStatus, jqXHR) {
    				that.setItemDetails(data);
    			}
    		});
    	},
    	
    	searchIssueDetails: function (itemId) {
    		$.ajax({
    			type: 'GET',
    			url: 'query',
    			data: {type: 'issueDetails', query: itemId},
    			dataType: 'json',
    			async: true,
    			success: function (data, textStatus, jqXHR) {
    				that.setIssueDetails(data);
    			}
    		});
    	},
    	
    	searchCommitDetails: function (itemUri) {
    		$.ajax({
    			type: 'GET',
    			url: 'query',
    			data: {type: 'commitDetails', query: itemUri},
    			success: function (data, textStatus, jqXHR) {
    				that.setCommitDetails(data);
    			}
    		});
    	},
    	
    	setQueryResults: function (data) {
    		if (data.type == 'peopleData') {
        		var nodeH = data.nodeH;
        		var nodeV = [];
        		for (var key in nodeH) {
        			var node = nodeH[key];
        			var neighbourIds = node.neighbours;
        			var neighbours = [];
        			for (var i = 0; i < neighbourIds.length; i++) {
        				neighbours.push(nodeH[neighbourIds[i]]);
        			}
        			node.neighbours = neighbours;
        			nodeV.push(node);
        		}
        		
        		data.nodes = nodeV;
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
                type: "GET",
                url: "query",
                data: {
                	type: queryType,
                	keywords: queryOpts.keywords,
                	concepts: queryOpts.concepts,
        			people: queryOpts.people,
        			sources: queryOpts.sources,
        			products: queryOpts.products,
        			issues: queryOpts.issues,
        			from: queryOpts.from,
        			to: queryOpts.to,
        			issuesChk: queryOpts.issuesChk,
        			commitsChk: queryOpts.commitsChk,
        			forumsChk: queryOpts.forumsChk,
        			mailsChk: queryOpts.mailingListsChk,
        			wikisChk: queryOpts.wikisChk
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
    		$.ajax({
                type: "GET",
                url: "query",
                data: {
                	type: 'itemData',
                	keywords: queryOpts.keywords,
                	concepts: queryOpts.concepts,
        			people: queryOpts.people,
        			sources: queryOpts.sources,
        			products: queryOpts.products,
        			from: queryOpts.from,
        			to: queryOpts.to,
        			issuesChk: queryOpts.issuesChk,
        			commitsChk: queryOpts.commitsChk,
        			forumsChk: queryOpts.forumsChk,
        			mailsChk: queryOpts.mailingListsChk,
        			wikisChk: queryOpts.wikisChk,
        			offset: offset,
        			maxCount: limit
                },
                dataType: "json",
                async: true,
                success: function (data, textStatus, jqXHR) {
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
    		var keywords = generalSearch.getSearchStr('keyword');
    		var concepts = generalSearch.getSearchStr('concept');
    		var people = generalSearch.getSearchStr('person');
    		var sources = generalSearch.getSearchStr('source');
    		var products = generalSearch.getSearchStr('product');
    		var issues = generalSearch.getSearchStr('issue');
    		
    		var from = $('#from_text').val();
    		var to = $('#to_text').val();
    		
    		var queryOpts = {
    			keywords: keywords,
    			concepts: concepts,
    			people: people,
    			sources: sources,
    			products: products,
    			issues: issues,
    			from: from,
    			to: to,
    			issuesChk: $('#issues_check').attr('checked') == 'checked',
    			commitsChk: $('#commits_check').attr('checked') == 'checked',
    			forumsChk: $('#forums_check').attr('checked') == 'checked',
    			mailingListsChk: $('#mailing_check').attr('checked') == 'checked',
    			wikisChk: $('#wikis_check').attr('checked') == 'checked'
    		};
			
			that.searchKeywordsGeneral(queryOpts);
			that.searchTimelineGeneral(queryOpts);
			that.searchItemsGeneral(queryOpts, 0, 100);
			that.searchPeopleGeneral(queryOpts);
			
			that.currentQueryOpts = queryOpts;
			that.cleanData();
			return false;
    	},
    	
    	searchIssueId: function (offset, limit) {
    		var issues = $('#issue_id_text').val();
    		
    		try {
	    		$.ajax({
	                type: "GET",
	                url: "query",
	                data: {
	                	type: 'duplicateIssue',
		    			issues: issues,
		    			unconfirmedChk: $('#unconfirmed_check').attr('checked') == 'checked',
		            	newChk: $('#new_check').attr('checked') == 'checked',
		            	assignedChk: $('#assigned_check').attr('checked') == 'checked',
		            	resolveChk: $('#resolved_check').attr('checked') == 'checked',
		            	invalidChk: $('#invalid_check').attr('checked') == 'checked',
		            	worksChk: $('#works_check').attr('checked') == 'checked',
		            	fixedChk: $('#fixed_check').attr('checked') == 'checked',
		            	wondChk: $('#wond_check').attr('checked') == 'checked',
		            	duplicateChk: $('#duplicate_check').attr('checked') == 'checked',
		            	offset: offset,
		            	limit: limit
	                },
	                dataType: "json",
	                async: true,
	                success: function (data, textStatus, jqXHR) {
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
    	
    	setIssueDetails: function (data) {
    		// generate accordion
    		// item description
    		var html = '<table class="heading"><tr>';
    		html += '<td class="title_desc">Item description</td>';
    		html += '<td>Author: <div id="author_desc" class="data">' + data.author.name + '</div></td>';
    		html += '<td>Date: <div id="date_desc" class="data">' + new Date(data.dateOpened).format(defaultDateFormat) + '</div></td>';
    		html += '<td>Resolution: <div id="resolution_desc" class="data">' + data.resolution + '</div></td>';
    		html += '<td>Status: <div id="status_desc" class="data">' + data.status + '</div></td>';
    		html += '</tr></table>';
    		// content
    		html += '<div class="content"><table id="item_details"><tr><td colspan="3"><div id="item-accordion">' + data.description + '</div></td></tr></table></div>';
    		
    		// comments
    		var comments = data.comments;
    		for (var i = 0; i < comments.length; i++) {
    			var comment = comments[i];
    			
    			html += '<table class="heading"><tr>';
    			html += '<td class="title_comm">Comment</td>';
    			html += '<td>Author: <div id="author_comm_' + i + '" class="data">' + comment.person.name + '</div></td>';
    			html += '<td>Date: <div id="date_comm_' + i + '" class="data">' + new Date(comment.commentDate).format(defaultDateFormat) + '</div></td>';
    			html += '</tr></table>';
    			// content
    			html += '<div class="content">' + comment.commentText + '</div>';
    		}
    		
    		// related issues TODO

    		// insert html
    		$('#details_wrapper').html(html);
    		jQuery(".content").hide();
    		jQuery(".heading").click(function() {
			    jQuery(this).next(".content").slideToggle(500);
			});
    		
    		/*
				<table class="heading" style="width:100%;">
				<tr>
				<td style="font-weight:bold;">
				Related issues
				</td>
				</tr>
				</table>
				<div class="content">Lorem ipsum dolor sit amet, consectetuer adipiscing elit orem ipsum dolor sit amet, consectetuer adipiscing elit</div>*/
    	},
    	
    	setCommitDetails: function (data) {
    		
    		// header
    		var html = '<table class="heading"><tr>';
    		html += '<td class="title_desc">Commit comment</td>';
    		
    		html += '<td>Committer: <div id="author_desc" class="data">' + data.committer.name + '</div></td>';	// TODO change class
    		html += '<td>Author: <div id="author_desc" class="data">' + data.author.name + '</div></td>';
    		html += '<td>Date: <div id="date_desc" class="data">' + new Date(data.commitDate).format(defaultDateFormat) + '</div></td>';
    		html += '<td>Revision: <div id="resolution_desc" class="data">' + data.revisionTag + '</div></td>';		// TODO change class
    		
    		html += '</tr></table>';
    		// content
    		html += '<div class="content"><table id="item_details"><tr><td colspan="3"><div id="item-accordion">' + data.message + '</div></td></tr></table></div>';
    		
    		// files
    		var files = data.files;
    		for (var fileIdx = 0; fileIdx < files.length; fileIdx++) {
    			var file = files[fileIdx];
    			var modules = file.modules;
    			
    			html += '<table class="heading"><tr>';
    			html += '<td class="title_comm">File</td>';
    			html += '<td>Name: <div id="name_file_' + fileIdx + '" class="data">' + file.name + '</div></td>';
    			html += '<td>Action: <div id="action_file_' + fileIdx + '" class="data">' + file.action + '</div></td>';
    			html += '</tr></table>';
    			// modules
    			html += '<div class="content"><table class="module_table">';
    			
    			for (var moduleIdx = 0; moduleIdx < modules.length; moduleIdx++) {
    				var module = modules[moduleIdx];
    				var methods = module.methods;
    				
    				html += '<tr>';
    				html += '<table>';
    				html +='<tr><td>Start line: ' + module.startLine + '</td><td>' + module.endLine + '</td></tr>';
    				html += '<tr><td colspan="2">Name: ' + module.name + '</td></tr>';
    				html += '<tr><td>Methods</td></tr>';
    				html += '<tr><td><ul class="methods_ul">';
    				for (var methodIdx = 0; methodIdx < methods.length; methodIdx++) {
    					var method = methods[methodIdx];
    					
    					html += '<li><table class="method_table">';
    					html += '<tr><td>Start line: ' + method.startLine + '</td><td>End line: ' + method.endLine + '</td></tr>';
    					html += '<tr><td colspan="2">' + method.moduleMethods + '</td></tr>';
    					html += '</table></li>';
    				}
    				html += '</ul></td></tr>';
    				html += '</table>';
    				html += '</tr>';
    			}
    			
    			html += '</table></div>';
    		}
    		
    		$('#details_wrapper').html(html);
    		jQuery(".content").hide();
    		jQuery(".heading").click(function() {
			    jQuery(this).next(".content").slideToggle(500);
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
    						var value = $(event.srcElement).text();
    		    			viz.addToSearchField('keyword_text', {type: 'keyword', label: value, value: value});
    					}
    			};
    		}
    		
    		$('#wordcloud-div').html('');
    		$('#wordcloud-div').jQCloud(data, {width: width, height: height});
    	},
    	
    	createTimeline: function(data) {
    		var allDays = data.days;
    		var allMonths = data.months;
    		var monthH = data.monthH;
    		
    		var history = ZoomHistory();
    		var selectedRange = [Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY];
    		var normalColor = getCssValue('bar-normal', 'background-color');
    		var selectedColor = getCssValue('bar-selected', 'background-color');
    		var ctrlDown = false;
    		$(document).keydown(function (event) {
    			if (event.keyCode == 17)	// CTRL
    				ctrlDown = true;
    		});
    		$(document).keyup(function (event) {
    			if (event.keyCode == 17)	// CTRL
    				ctrlDown = false;
    		});
    		    		
    		// create a series for the timeline
    		var monthOffsetH = {};
    		var seriesV = [];
    		var dayIdx = 0;
    		for (var i = 0; i < allMonths.length; i++) {
    			var monthLabel = allMonths[i];
    			var monthIdx = parseInt(monthLabel.split('-')[1]) - 1;
    			var monthLength = monthLengths[monthIdx];
    			var firstDayIdx = dayIdx;
    			monthOffsetH[monthLabel] = dayIdx;
    			
    			var nDays = 0;
    			for (var j = 0; j < monthLength; j++)
    				nDays += allDays[dayIdx++];
    			
    			seriesV.push({name: monthLabel, y: nDays, id: firstDayIdx + '-' + (dayIdx - 1)});
    		}
    		
    		function getPointColor(year, month) {
    	    	var fromDate = $('#from_text').datepicker('getDate');
    	    	var toDate = $('#to_text').datepicker('getDate');
    	    	
    	    	if (fromDate == null || toDate == null)
    	    		return normalColor;
    	    	
    	    	var color = selectedColor;
    			if (fromDate == null || toDate == null)
    				color = normalColor;
    			else if (year < fromDate.getFullYear() || (year == fromDate.getFullYear() && month < fromDate.getMonth() + 1))
    				color = normalColor;
    			else if (year > toDate.getFullYear() || (year == toDate.getFullYear() && month > toDate.getMonth() + 1))
    				color = normalColor;
    			
    			return color;
    	    }
    		
    		var minX = 0;	var maxX = seriesV.length;
    		
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
								
								var range = [event.xAxis[0].min, event.xAxis[0].max];
								var data = chart.series[0].data;
								
								var first = true;
								var last = true;
								var firstPoint = null;
								var lastPoint = null;
								for (var i = 0; i < data.length; i++) {
									var point = data[i];
									var x = point.x;
									
									if (x >= range[0] && first) {
										first = false;
										var dayIdx = parseInt(point.id.split('-')[0]);
										selectedRange[0] = dayIdx;
										firstPoint = point;
									}
									if (x >= range[1] && last) {
										last = false;
										var dayIdx = parseInt(point.id.split('-')[1]) - 1;
										selectedRange[1] = dayIdx;
										lastPoint = data[i-1];
									}
									
									var color = x < range[0] || x > range[1] ? normalColor : selectedColor;
									point.update({color: color}, false);
								}
								
								if (lastPoint == null) { 
									lastPoint = data[data.length - 1];
									var dayIdx = parseInt(lastPoint.id.split('-')[1]) - 1;
									selectedRange[1] = dayIdx;
								}
								
								// set dates in the form
								if (firstPoint != null && lastPoint != null) {
									var firstLabel = firstPoint.name;
									var lastLabel = lastPoint.name;
									
									var firstLabelV = firstLabel.split('-');
									var lastLabelV = lastLabel.split('-');
									
									// create the dates
									var firstDate = new Date();
									var lastDate = new Date();
									
									var lastMonthIdx = parseInt(lastLabelV[1]) - 1;
									var lastMonthLength = monthLengths[lastMonthIdx];
									
									firstDate.setFullYear(parseInt(firstLabelV[0]), parseInt(firstLabelV[1]) - 1, 1);
									lastDate.setFullYear(parseInt(lastLabelV[0]), lastMonthIdx, lastMonthLength);
									
									$('#from_text').datepicker("setDate", firstDate);
									$('#to_text').datepicker("setDate", lastDate);
									
									$('#from_text').change();
									$('#to_text').change();
								}
							}
						}
					}
				},
				colors: [normalColor],
				title: null,				
				xAxis: {
					min: minX,
					max: maxX,
					minRange: 40/allMonths.length,	// 40 is the default range
					endOnTick: false,
					tickInterval: (maxX - minX)/seriesV.length,
					categories: allMonths,
					labels: {
						rotation: -45,
						align: 'right',
						style: {
							 font: 'normal 10px Arial, sans-serif'
						}
					},
					events: {
						setExtremes: function (event) {
							var min = event.min;
							var max = event.max;
							
							// update the zooming history
							if (min == null && max == null)
								history.clear();
							else
								history.addItem(min, max);
							
							// process the event
							if (min == null && max == null) {
								var data = [];
								for (var i = 0; i < seriesV.length; i++) {
									var point = seriesV[i];
									var label = point.name;
									
									var yearMonthV = label.split('-');
									var year = parseInt(yearMonthV[0]);
									var month = parseInt(yearMonthV[1]);
									
									var color = getPointColor(year, month);

									data.push({name: label, y: point.y, id: point.id, color: color});
								}
								
								chart.series[0].setData(data);
							} else {
								// store the months that will be in the chart
								var selectedMonths = [];
								var allMonths = [];
								var monthSet = {};
								jQuery.each(chart.series[1].data, function (i, point) {
									if (!monthSet[point.category]) {
										if (point.x >= min && point.x < max) {
											selectedMonths.push(point.category);
										}
										allMonths.push(point.category);
										monthSet[point.category] = true;
									}
								});
								
								// display just the current months and their left and right neighbours
								// the months are already ordered
								var daysPerCol = selectedMonths.length;
								
								// prevent infinite loop
								if (daysPerCol < 1)
									return true;
								
								// add the neighbours and get the first day index, so I can
								// compute the offset
								var beginIdx = allMonths.indexOf(selectedMonths[0]);
								var endIdx = allMonths.indexOf(selectedMonths[selectedMonths.length-1]);
								var firstDayIdx = 0;
								if (endIdx < allMonths.length - 1)
									selectedMonths.push(allMonths[endIdx + 1]);
								if (beginIdx > 0) {
									selectedMonths.unshift(allMonths[beginIdx - 1]);
									for (var i = 0; i < beginIdx - 1; i++)
										firstDayIdx += monthH[allMonths[i]].length;
								}
								
								// create new columns
								var monthW = (maxX - minX)/seriesV.length;
								var dx = (maxX - minX)/allDays.length;
								var offset = dx*firstDayIdx - .5*monthW;
								var zoomDat = [];
								
								
								var days = [];
								for (var monthIdx = 0; monthIdx < selectedMonths.length; monthIdx++) {
									var label = selectedMonths[monthIdx];
									var month = monthH[label];
									
									for (var i = 0; i < month.length; i++)
										days.push([month[i], label]);

								}
								
								var daysOffset = monthOffsetH[selectedMonths[0]];
								var dayIdx = 0;
								while (dayIdx < days.length) {
									var x = offset + dayIdx*dx;
									var y = 0;
									
									var first = daysOffset + dayIdx;
									
									var label = days[Math.min(days.length - 1, dayIdx + Math.floor(daysPerCol/2))][1];
									var monthV = label.split('-');
									var color = getPointColor(parseInt(monthV[0]), parseInt(monthV[1]));
									
									for (var i = 0; i < daysPerCol && dayIdx < days.length; i++)
										y += days[dayIdx++][0];
									
									var last = daysOffset + dayIdx - 1;
																		
									zoomDat.push({x: x, y: y, name: label, color: color, id: first + '-' + last});
								}

								chart.series[0].setData(zoomDat);
							}
							return true;
						}
					}
				},
				yAxis: {
					min: 0,
					title: {
						text: 'Posts'
					}
				},
				legend: {
					enabled: false
				},
				tooltip: {
					formatter: function() {
						return '<b>'+ this.point.name +'</b><br/>'+
							 'Number of posts: ' + Highcharts.numberFormat(this.y, 0);
							 
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
						data: seriesV,
						dataLabels: {
							enabled: false,
							rotation: -90,
							color: '#FFFFFF',
							align: 'right',
							x: -5,
							y: 10,
							formatter: function() {
								return this.y;
							},
							style: {
								font: 'normal 12px Arial, sans-serif'
							}
						}			
					},
					{
						name: 'Posts',
						data: seriesV,
						dataLabels: {
							enabled: false,
							rotation: -90,
							color: '#FFFFFF',
							align: 'right',
							x: -5,
							y: 10,
							formatter: function() {
								return this.y;
							},
							style: {
								font: 'normal 12px Arial, sans-serif'
							}
						}		
					}
				]
			});
			
			chart.selectDates = function (beginDate, endDate) {
				var data = chart.series[0].data;
				for (var i = 0; i < data.length; i++) {
					var point = data[i];
					var label = point.name;
					
					var yearMonthV = label.split('-');
					var month = parseInt(yearMonthV[1]);
					var year = parseInt(yearMonthV[0]);
					
					var color = getPointColor(year, month);
					point.update({color: color}, false);
				}
			};
			
			chart.series[1].hide();
			
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
    				
    				html += '<td class="item_header">' + peopleH[item.senderID].name + ' to ' + toNameList.join(', ') + '</td>';
    				
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
    				html += '<div class="item-wrapper issue" onclick="viz.searchIssueDetails(' + item.id + ')"><table class="item_table">';
					
    				// from + date
					html += '<tr>';
					html += '<td class="item_header">' + peopleH[item.senderID].name + '</td>';
					html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
	    			
					// subject + similarity
					html += '<tr>';
					if (item.similarity != null) {
						if (item.url != null)
							html += '<td class="item_subject"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
						else
							html += '<td class="item_subject">' + item.subject + '</td>';
						
						// similarity
						if (item.similarity != null)
							html += '<td class="item_similarity">sim: ' + item.similarity + '</td>';
					}
					else {
						if (item.url != null)
							html += '<td colspan="2" class="item_subject"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
						else
							html += '<td colspan="2" class="item_subject">' + item.subject + '</td>';
						
					}
					html += '</tr>';
					
					// content
					html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
	    			html += '</table></div>';
    				break;
    			case Type.bugComment:
    				html += '<div class="item-wrapper comment" onclick="viz.searchIssueDetails(' + item.issueID + ')"><table class="item_table">';
    				
    				// from + date
    				html += '<tr>';
					html += '<td class="item_header">' + peopleH[item.senderID].name + '</td>';
					html += '<td class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
					html += '</tr>';
					
					// subject + similarity
					html += '<tr>';
					if (item.similarity != null) {
						if (item.url != null)
							html += '<td class="item_subject"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
						else
							html += '<td class="item_subject">' + item.subject + '</td>';
						
						// similarity
						if (item.similarity != null)
							html += '<td class="item_similarity">sim: ' + item.similarity + '</td>';
					}
					else {
						if (item.url != null)
							html += '<td colspan="2" class="item_subject"><a href="' + item.url + '" target="_blank">' + item.subject + '</a></td>';
						else
							html += '<td colspan="2" class="item_subject">' + item.subject + '</td>';
						
					}
					html += '</tr>';
    				
					// content
					html += '<tr><td colspan="2" class="item_content">' + item.content + '</td></tr>';
	    			html += '</table></div>';
    				break;
    			case Type.commit:
    				html += '<div class="item-wrapper commit" onclick="viz.searchCommitDetails(\'' + item.entryID + '\')"><table class="item_table">';
    				
    				// author + date
    				html += '<tr>';
					html += '<td colspan="2" class="item_header">' + peopleH[item.authorID].name + '</td>';
					html += '<td colspan="2" class="item_date">' + new Date(item.time).format(defaultDateFormat) + '</td>';
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
    		
    		var info = data.info;
    		var navHtml = (info.offset + 1) + ' to ' + (info.offset + data.items.length) + ' of ' + info.totalCount;
    		if (info.offset > 0)
    			navHtml = '<a onclick="viz.searchItemsGeneral(viz.currentQueryOpts, ' + (info.offset - info.limit) + ', ' + info.limit + ')">&lt;&lt;</a> ' + navHtml;
    		if (info.offset + info.limit < info.totalCount)
    			navHtml += ' <a onclick="viz.searchItemsGeneral(viz.currentQueryOpts, ' + (info.offset + info.limit) + ', ' + info.limit + ')">&gt;&gt;</a>';
    		
    		html += '<li id="nav">' + navHtml + '</li>';

    		html += '</ul>';
    		$('#items-div').html(html);
    	},

    	createGraph: function(data) {
    		if (that.socialGraph == null) {
	    		that.socialGraph = SocialGraph({
	    			step: 10,
	    			container: '#graph-div',    			
	    		});
    		}
    		that.socialGraph.init(data);
    	},
    	
    	decreaseGraph: function() {
    		if(that.socialGraph)
    			that.socialGraph.showLess();
    	},
    	
    	increaseGraph: function() {
    		if(that.socialGraph)
    			that.socialGraph.showMore();
    	}
    };
    
    
    // init the search text fields
    // general search  
    $('#keyword_text').autoSuggest('suggest', {
    	selectedItemProp: 'label',
    	searchObjProps: 'label',
    	neverSubmit: true,
    	showResultList: false,
    	startText: 'keywords,...',
    	asHtmlID: 'keyword_text',
    	selectionAdded: function(elem, data) {
    		if (!settingManually) {
	    		generalSearch.addToSearch(data);
	    		updateUrl();
    		}
    	},
	  	selectionRemoved: function(elem) {
	  		generalSearch.removeFromSearch(elem);
	  		updateUrl();
	  	}
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
    		if (!settingManually) {
	    		generalSearch.addToSearch(data);
	    		updateUrl();
    		}
    	},
	  	selectionRemoved: function(elem) {
	  		generalSearch.removeFromSearch(elem);
	  		updateUrl();
	  	},
	  	selectionClick: function (item, input) {
	  		if (!$(item).hasClass('person')) 
	  			return;
	  		
	  		$('#popup_search_td').html('<input type="text" id="or_text" class="text" />');
    		
    		var currentLabel = $(item).text().substring(1);
    		
    		var idx = generalSearch.indexOfLabel('person', currentLabel);
    		if (idx < 0) return;
    		
    		// construct a list of objects which will be edited
    		var selectedObj = generalSearch.getObjByLabel('person', currentLabel);
    		var labels = selectedObj.label.split(',');
    		var values = selectedObj.value.split('|');
    		var selectedObjs = [];
    		for (var i = 0; i < labels.length; i++)
    			selectedObjs.push({label: labels[i], value: values[i], type: selectedObj.type});
    		
    		// open a popup to search on new keywords
    		$('#or_text').autoSuggest('suggest', {
    			selectedItemProp: 'label',
    	    	searchObjProps: 'label',
    	    	selectedValuesProp: 'value',
    	    	queryParam: 'People',
    	    	retrieveLimit: false,
    	    	neverSubmit: true,
    	    	startText: '',
    	    	addByWrite: false,
    	    	asHtmlID: 'or_text',
    	    	preFill: selectedObjs,
    	    	selectionAdded: function (elem, data) {
    	    		generalSearch.addOrTerm('person', idx, data);
    	    	},
    	    	selectionRemoved: function (elem, data) {
    	    		generalSearch.removeTerm('person', data);
    	    		elem.remove();
    	    	}
    		});
    		
    		$('#popup_search').bPopup({
    			onClose: function () {
    				var values = $('#as-values-or_text').val().replace(/^,|,$/g, '');	// same as trimming commas
    				if (values.length > 0) {
    					var currentTerms = generalSearch.getTypeV('person');
    					
    					var newValue = currentTerms[idx].value;
    					var displayStr = currentTerms[idx].label;
    					
    					var close = $('<a class="as-close">&times;</a>').click(function(){
							input.values_input.val(input.values_input.val().replace(newValue + ",",""));
							input.opts.selectionRemoved.call(this, item);
							input.input_focus = true;
							input.focus();
							return false;
						});
    					
    					var oldValue = $(item).text().substring(1);
    					item.html(displayStr).prepend(close);
    					
    					// replace the current value in the hidden field
    					// first build the current value
    					oldValue = oldValue.replace(/,/g, '\|');
    					var inputValue = $('#as-values-other_text').val();
    					if (inputValue.search(new RegExp('^' + oldValue + ',')) >= 0) {
    						// the value is at the begining of the line
    						$('#as-values-other_text').val(inputValue.replace(new RegExp('^' + oldValue + ','), newValue + ','));
    					} else {
    						// somewhere in the middle
    						// a comma is always at the end
    						$('#as-values-other_text').val(inputValue.replace(new RegExp(',' + oldValue + ','), ',' + newValue + ','));
    					}
    				}
    			}
    		});
	  	}
    });
    
    function setRange() {
    	var fromDate = $('#from_text').datepicker('getDate');
    	var toDate = $('#to_text').datepicker('getDate');
    	
    	if (fromDate != null && toDate != null && chart != null) {
	    	chart.selectDates(fromDate, toDate);
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
    	}
    });
    
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
	  	selectionRemoved: function(elem) {
	  		personSearch.removeFromSearch(elem);
	  		updateUrl();
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
    
    return that;
};
