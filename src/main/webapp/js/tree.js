(function($){
	$.fn.tree = function (config) {
		var target = this[0];
		var toggleTime = config.toggleTime == null ? 500 : config.toggleTime;
		
		$(target).find('.tree_li div.toggle').next().hide();
		
		// add a link nudging animation effect to each link
	    $(target).find('.tree_li div.toggle').hover(function() {
	        $(this).stop().animate( {
				fontSize: config.focusSize || '110%'
	        }, 300);
	    }, function() {
	        $(this).stop().animate( {
				fontSize: '100%'
	        }, 300);
	    });
		
		// prepend a plus sign to signify that the sub-menus aren't expanded
		$(target).find(".tree_li div.toggle").prepend("+ ");
		
		// add a click function that toggles the sub-menu when the corresponding
		// span element is clicked
		$(target).find(".tree_li div.toggle").click(function() {
			$(this).next().toggle(toggleTime);
			
			// switch the plus to a minus sign or vice-versa
			var v = $(this).html().substring(0, 1);
			if ( v == "+" )
				$(this).html( "-" + $(this).html().substring( 1 ) );
			else if ( v == "-" )
				$(this).html( "+" + $(this).html().substring( 1 ) );
		});
	};
})(jQuery);