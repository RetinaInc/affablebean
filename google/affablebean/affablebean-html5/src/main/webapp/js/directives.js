'use strict';

/* Directives */


angular.module('myApp.directives', []).
	directive('cartItems', function() {
		return {
			template: '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{{cart.items}} item(s)'
		};
	});
