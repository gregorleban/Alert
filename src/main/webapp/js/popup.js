(function($){
	$.fn.popup = function (config) {
		var popup = this[0];
		var trigger = config.trigger;
		var align = config.align || 'center';
		var fadeDuration = config.duration || 'fast';
		var verticalOffset = config.verticatOffset == null ? 2 : config.verticatOffset;
		var eventType = config.event || 'click';
		
		$(popup).css('display', 'none');
		$(popup).css('z-index', '100');
		$(popup).css('position', 'absolute');
		
		$(trigger).bind(eventType, function (event) {
			event.stopPropagation();
			event.preventDefault();
			
			var triggerPos = $(trigger).position();	// relative to the parent
			var trigerBottom = triggerPos.top + $(trigger).height();
			
			var left;
			var top = trigerBottom + verticalOffset;;
			if (align == 'center')
				left = triggerPos.left + ($(trigger).width() - $(popup).width())/2;
			else if (align == 'right')
				left = triggerPos.left + $(trigger).width() - $(popup).width();
			else
				left = triggerPos.left;

			$(popup).css('left', left);
			$(popup).css('top', top);
			if ($(popup).css('display') == 'none')
				$(popup).fadeToggle(fadeDuration);
			
			return false;
		});
		$(popup).mouseout(function (event) {
			if ($(popup).css('display') != 'none')
				$(popup).fadeToggle(fadeDuration);
		});
		$(document).click(function (event) {
			if (event.target != $(popup) && event.target != $(trigger) && $(popup).css('display') != 'none')
				$(popup).fadeToggle(fadeDuration);
		});
	};
})(jQuery);