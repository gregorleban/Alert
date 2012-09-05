(function(){
  $.fn.popbox = function(options){
    var settings = $.extend({
      selector      : this.selector,
      open          : '.open',
      box           : '.box',
      arrow         : '.arrow',
      arrow_border  : '.arrow-border',
      close         : '.close'
    }, options);
    
    function close(event) {
    	if(!$(event.target).closest(settings['box']).length){
            methods.close();
          }
    }
    
    var methods = {
      open: function(event){
    	  $(document).bind('click', close);
    	  event.preventDefault();
    	  event.stopPropagation();
        var pop = $(this);
        var box = $(this).parent().find(settings['box']);
        box.find(settings['arrow']).css({'left': box.width()/2 - 10});
        box.find(settings['arrow_border']).css({'left': box.width()/2 - 10});
        if(box.css('display') == 'block'){
          methods.close();
        } else {
          box.css({'display': 'block', 'top': 40, 'left': -107});
        }
      },
      close: function(){
    	  $(document).unbind('click', close);
        $(settings['box']).fadeOut("fast");
      }
    };
    $(document).bind('keyup', function(event){
      if(event.keyCode == 27){
        methods.close();
      }
    });
    
    return this.each(function(){
      $(this).css({'width': $(settings['box']).width()});
      $(settings['open'], this).bind('click', methods.open);
      $(settings['open'], this).parent().find(settings['close']).bind('click', function(event){
        event.preventDefault();
        methods.close();
      });
    });
  };
}).call(this);