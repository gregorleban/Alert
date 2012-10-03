(function($){
	$.fn.tree = function (config) {
		var target = this[0];
		
		$(target).find('.tree_li span.toggle').next().hide();
		
		// add a link nudging animation effect to each link
	    $(target).find('.tree_li span.toggle').hover(function() {
	        $(this).stop().animate( {
				fontSize:"15px",
				color:"black"
	        }, 300);
	    }, function() {
	        $(this).stop().animate( {
				fontSize:"14px",
				color:"#808080"
	        }, 300);
	    });
	    
	    // set the cursor of the toggling span elements
		$(target).find('span.toggle').each(function (idx, el) {
			$(el).css('cursor', 'pointer');
		});
		
		// prepend a plus sign to signify that the sub-menus aren't expanded
		$(target).find(".tree_li .toggle").each(function (idx, el) {
			$(el).prepend("+ ");
		});
		
		// add a click function that toggles the sub-menu when the corresponding
		// span element is clicked
		$(target).find(".tree_li .toggle").click(function() {
			$(this).next().toggle(1000);
			
			// switch the plus to a minus sign or vice-versa
			var v = $(this).html().substring(0, 1);
			if ( v == "+" )
				$(this).html( "-" + $(this).html().substring( 1 ) );
			else if ( v == "-" )
				$(this).html( "+" + $(this).html().substring( 1 ) );
		});
	};
})(jQuery);