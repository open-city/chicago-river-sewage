function setBarWidthByCurrency(col_class) {
	$('.bar span strong').toNumber();
	calculateNumberBars();
	$('.bar span strong').formatCurrency({ roundToDecimalPlace:0 });
}

function setBarWidthByNumber(col_class) {
	calculateNumberBars(col_class);
}

function calculateNumberBars(col_class)
{
	var maxArray = new Array();
    $('.' + col_class + '.bar span strong').each(function(){
      maxArray.push(parseInt($(this).html()));
    });

    var maxNumber = Math.max.apply( Math, maxArray );
    if (maxNumber > 0)
    {
      $('.' + col_class + '.bar').each(function(){
        $(this).children().width((($(this).children().children().html()/maxNumber) * 100) + '%');
      });
    }
    else {
    	$('.' + col_class + ' .bar').each(function(){
        	$(this).children().width('0%');
      	});
    }
}