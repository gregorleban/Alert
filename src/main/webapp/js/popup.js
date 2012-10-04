(function($){
	$.fn.popup = function (config) {
		var popup = this[0];
		var trigger = config.trigger;
		var align = config.align || 'center';
		var fadeDuration = config.duration || 'fast';
		var verticalOffset = config.top == null ? 2 : config.top;
		var horizontalOffset = config.left == null ? 0 : config.left;
		var eventType = config.event || 'click';
		
		$(popup).css('display', 'none');
		$(popup).css('z-index', '100');
		$(popup).css('position', 'absolute');
		
		$(trigger).bind(eventType, function (event) {
			event.stopPropagation();
			event.preventDefault();
			
			var triggerPos = $(trigger).position();	// relative to the parent
			var trigerBottom = triggerPos.top + $(trigger).height();
			
			var left = horizontalOffset;
			var top = trigerBottom + verticalOffset;;
			if (align == 'center')
				left += triggerPos.left + ($(trigger).outerWidth() - $(popup).outerWidth())/2;
			else if (align == 'right')
				left += triggerPos.left + $(trigger).outerWidth() - $(popup).outerWidth();
			else
				left += triggerPos.left;

			$(popup).css('left', left);
			$(popup).css('top', top);
			if ($(popup).css('display') == 'none')
				$(popup).fadeToggle(fadeDuration);
			
			return false;
		});
		/*$(popup).mouseleave(function (event) {
			if ($(popup).css('display') != 'none')
				$(popup).fadeToggle(fadeDuration);
		});*/
		$(document).click(function (event) {
			if (event.target != $(popup) && event.target != $(trigger) && $(popup).css('display') != 'none' && $.inArray(event.target, $(popup).find('*')) < 0)
				$(popup).fadeToggle(fadeDuration);
		});
	};
})(jQuery);