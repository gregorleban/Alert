
function DynamicGraph (options) {
	var width = options.width;
	var height = options.height;
	
	// init the stage
	var stage = new Kinetic.Stage({
		container: options.container,
		width: width,
		height: height
	});
	
	var nodeLayer = new Kinetic.Layer();
	stage.add(nodeLayer);
	
	// add handlers
	for (var handler in options.handlers.stage)
		stage.on(handler, options.handlers.stage[handler]);
	
	// create the particle system
	var particleSystem = arbor.ParticleSystem(2000, 50, 0.5, true);
	particleSystem.parameters({gravity:true});
	particleSystem.parameters({precision:0.9});
	
	// structures
	var selectedNodes = {};
	var nodeH = {};
	var displayedNodes = {};
	var displayedEdges = {};
	var rankedNodeNames = [];
	
	// variables
	var totalNodes = 0;
	var startDisplayLevel = options.startDisplayLevel == null ? 50 : options.startDisplayLevel;
	var	minDisplayedNodes = options.minNodes == null ? 3 : options.minNodes;
	
	var currentDisplayLevel = null;
	var step = options.step == null ? 5 : options.step;
	
	var that = {
		addNode: function (node) {
			if (!(node.id in displayedNodes)) {
				nodeLayer.add(node.prop);
				if (particleSystem.getNode(node.id) == null)
					node.sysNode = particleSystem.addNode(node.id, node.data);
				
				displayedNodes[node.id] = node;
			}
		},
		
		addEdge: function (edge) {
			var source = edge.source;
			var target = edge.target;
			if (source.id in displayedNodes && target.id in displayedNodes && !(edge.id in displayedEdges)) {
				nodeLayer.add(edge.prop);
				displayedEdges[edge.id] = edge;
				
				if (particleSystem.getEdges(source.id, target.id).length == 0)
					particleSystem.addEdge(source.id, target.id, edge.data);
			}
		},
		
		drawProps: function () {
			nodeLayer.draw();
		},
		
		draw: function () {
			stage.draw();
		},
		
		clear: function () {
			nodeLayer.removeChildren();
			
			for (var key in displayedNodes)
				particleSystem.pruneNode(displayedNodes[key].sysNode);
			
			selectedNodes = {};
			nodeH = {};
			displayedNodes = {};
			displayedEdges = {};
			totalNodes = 0;
			
			that.draw();
		},
		
		display: function (n) {
			// first clear the stage
			nodeLayer.removeChildren();
			displayedNodes = {};
			displayedEdges = {};
			
			currentDisplayLevel = n;
			
			// add nodes and edges
			// add nodes, the edges will be added later, otherwise some will be omitted
			var nodesToDisplay = rankedNodeNames.slice(0, n);
			var edgesToDisplay = [];
			for (var i = 0; i < nodesToDisplay.length; i++) {
				var node = nodeH[nodesToDisplay[i]];
				var edges = node.edges;
				
				that.addNode(node);
				edgesToDisplay = edgesToDisplay.concat(edges);
			}
			
			// add the edges
			for (var i = 0; i < edgesToDisplay.length; i++)
				that.addEdge(edgesToDisplay[i]);
			
			nodeLayer.draw();
		},
		
		showMore: function () {
			that.display(Math.min(totalNodes, currentDisplayLevel + step));
		},
		
		showLess: function () {
			var oldDisplayLevel = currentDisplayLevel;
			
			that.display(Math.max(minDisplayedNodes, currentDisplayLevel - step));
			
			var newDisplayLevel = currentDisplayLevel;
			var nodesToRemove = rankedNodeNames.slice(newDisplayLevel, oldDisplayLevel);
			for (var i = 0; i < nodesToRemove.length; i++)
				particleSystem.pruneNode(nodeH[nodesToRemove[i]].sysNode);
		},
		
		setData: function (data) {	
			that.clear();
			var allNodes = data.nodes;
			var allEdges = data.edges;
			
			var nodeDegrees = {};
			
			// rank the nodes
			for(var i = 0; i < allEdges.length; i++) {
				var edge = allEdges[i];
				var count = edge.data.count;
				
				if (edge.source == edge.target)
					nodeDegrees[edge.source] = (edge.source in nodeDegrees) ? nodeDegrees[edge.source] + count : count;
				else {
					nodeDegrees[edge.source] = (edge.source in nodeDegrees) ? nodeDegrees[edge.source] + count : count;
					nodeDegrees[edge.target] = (edge.target in nodeDegrees) ? nodeDegrees[edge.target] + count : count;
				}
			}
			
			var degNodeList = [];
			for(var i = 0; i < allNodes.length; i++) {
				var id = allNodes[i].id;
				var deg = 0;
				if(id in nodeDegrees){
					deg = nodeDegrees[id];
				}
				degNodeList.push([deg, id]);				
			}
			degNodeList.sort(function (a, b) {return b[0] - a[0];}); // sort in reverse order
			
			rankedNodeNames = [];
			for(var i = 0; i < degNodeList.length; i++)
				rankedNodeNames.push(degNodeList[i][1]);
			
			nodeH = {};
//			var edgeH = {};
			
			// construct a node hash
			for (var i = 0; i < allNodes.length; i++) {
				var node = allNodes[i];
				node.neighboursSelected = 0;
				node.selected = false;
				
				var propNode = Node({
					id: node.id,
					data: node,
					draggable: options.draggable,
					selectionMode: options.selectionMode,
					graph: that,
					drawFunc: options.drawNode,
					sysNode: particleSystem.addNode(node.id, node),
					handlers: options.handlers.node
				});
				
				propNode.data.prop = propNode.prop;
				nodeH[node.id] = propNode;
			}
			
			// construct an edge hash
			for (var i = 0; i < allEdges.length; i++) {
				var edge = allEdges[i];
				var source = nodeH[edge.source];
				var target = nodeH[edge.target];
				
				var propEdge = Edge({id: i + '',
					data: edge.data,
					drawFunc: options.drawEdge,
					source: source,
					target: target
				});
				
				propEdge.data.source = source;
				propEdge.data.target = target;
				
				if (source.edges.indexOf(propEdge) < 0) source.edges.push(propEdge);
				if (target.edges.indexOf(propEdge) < 0) target.edges.push(propEdge);
			}
			
			// remove all the nodes from the particle system
			for (var key in nodeH) {
				var sysNode = nodeH[key].sysNode;
				particleSystem.pruneNode(sysNode);
			}
			
			totalNodes = allNodes.length;
			that.display(startDisplayLevel);
		},
		
		getWidth: function () {
			return width;
		},
		
		getHeight: function () {
			return height;
		},
		
		setSize: function (newWidth, newHeight) {
			width = newWidth;
			height = newHeight;
			stage.setSize(width, height);
			particleSystem.screenSize(width, height);
			particleSystem.start();
			stage.draw();
		},
		
		getParticleSystem: function () {
			return particleSystem;
		},
		
		getSelectedNodes: function () {
			return selectedNodes;
		},
		
		getDisplayedNodes: function () {
			return displayedNodes;
		}
	};
	
	
	particleSystem.renderer = ArborRenderer({
		graph: that
	});
	
	return that;
}

function Node(opts) {
	var graph = opts.graph;
	var handlers = opts.handlers;
	var drawFunc = opts.drawFunc;
	
	var that = {
		id: opts.id,
		data: opts.data,
		sysNode: opts.sysNode,
		edges: [],
		prop: null,
		
		select: function (select) {
			var data = that.data;
			var selectedNodes = graph.getSelectedNodes();
			
			if (select == data.selected) return;
			
			data.selected = select;
			if (select)
				selectedNodes[that.data.id] = that;
			else
				delete selectedNodes[that.data.id];
			
			var neighbours = data.neighbours;
			for (var i = 0; i < neighbours.length; i++) {
				var neigh = neighbours[i];
				if (select) {
					neigh.neighboursSelected++;
					if (neigh.id in graph.getDisplayedNodes())
						neigh.prop.moveToTop();
				} else if(neigh.neighboursSelected > 0) {
					neigh.neighboursSelected--;
				}
			}
			
			// if using single selection mode => unselect prevoius nodes
			if (opts.selectionMode == 'single') {
				for (var nodeId in selectedNodes) {
					if (nodeId != that.data.id && nodeId in selectedNodes) {
						var node = selectedNodes[nodeId];
						node.data.selected = false;
						
						var neighbours = node.data.neighbours;
						for (var i = 0; i < neighbours.length; i++) {
							if (neighbours[i].neighboursSelected > 0)
								neighbours[i].neighboursSelected--;
						}
						
						delete selectedNodes[nodeId];
					}
				}
			}
			
			that.prop.moveToTop();
			graph.drawProps();
		},
		
		addHandler: function (eventName, handler) {
			that.prop.on(eventName, function (event) {
				handler(event, that);
			});
		},
		
		init: function () {
			that.prop = new Kinetic.Shape({
				drawFunc: function () {
					if (that.data.pos == null) return;
					var context = this.getContext();
					drawFunc(context, that.data);
				}
			});
			
			if (opts.draggable) {
				that.prop.draggable(true);
				that.prop.on('dragmove', function (event) {
					event.cancelBubble = true;
	
					var pos = that.prop.getPosition();
					var s = arbor.Point(pos.x, pos.y);
					var sys = graph.getParticleSystem();;
					var p = sys.fromScreen(s);
	
					that.sysNode.p = arbor.Point(p.x,p.y);
				});
				that.prop.on('dragstart', function (event) {
					event.cancelBubble = true;
					that.sysNode.fixed = true;
					that.select(true);
				});
				that.prop.on('dragend', function (event) {
					event.cancelBubble = true;
					that.sysNode.fixed = false;
				});
			}
			
			if (opts.selectionMode == 'single' || opts.selectionMode == 'multiple') {
				that.prop.on("click", function (event) {
					event.cancelBubble = true;
					that.select(!that.data.selected);
				});
			}
			
			// add other handlers
			var handNames = Object.keys(handlers);
			for (var i = 0; i < handNames.length; i++) {
				var hName = handNames[i];
				that.addHandler(hName, handlers[hName]);
			}
		}
	};
	
	that.init();
	
	return that;
};

function Edge(opts) {
	var that = {
		id: opts.id,
		data: opts.data,
		drawFunc: opts.drawFunc,
		source: opts.source,
		target: opts.target,
		
		prop: null,
		
		init: function () {
			that.prop = new Kinetic.Shape({
				drawFunc: function () {
					var context = this.getContext();
					that.drawFunc(context, that.data);
				}
			});
		}
	};
	
	that.init();
	return that;
}

function ArborRenderer(opts) {
	var graph = opts.graph;
	var sys = null;
	
	var that = {
		init: function(system) {
			sys = system;
			sys.screenSize(graph.getWidth(), graph.getHeight()); 
			sys.screenPadding(40);
		},
		
		redraw:function() {
			if (!sys) return;

			sys.eachNode(function(node, pt) {
				node.data.pos = pt;
				
				if (!node.data.prop.isDragging())
					node.data.prop.setPosition(pt.x, pt.y);
				else {	// fix
					var pos = node.data.prop.getPosition();
					var s = arbor.Point(pos.x, pos.y);
					var p = sys.fromScreen(s);
					
					node.p = p;
				}
			});

			// draw the edges
			sys.eachEdge(function(edge, pt1, pt2){
				//adjust the edge opacity based on the number of times it appears
				edge.data.source = edge.source.data;
				edge.data.target = edge.target.data;
				edge.data.pos1 = pt1;
				edge.data.pos2 = pt2;
			});
			
			graph.drawProps();
		}
	};
	
	return that;
}