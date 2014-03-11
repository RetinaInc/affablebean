'use strict';

/* Services */


// Demonstrate how to register services
angular.module('myApp.services', []).
	service('global', function() {
		var categories = {};
		var catProms = {};
		var confirmation = {};
		var prevLoc = {};
		var prodProms = {};
		var sale = {};
		var selectedCat = {};
		var surcharge = {};

		function IsNumeric(input) {
			return (input - 0) ==
				input && ('' + input).replace(/^\s+|\s+$/g, "").length > 0;
		}

		return {
			getCategories: function() {
				return categories;
			},
			setCategories: function(value) {
				categories = value;
			},
			getCatProms: function() {
				return catProms;
			},
			setCatProms: function(value) {
				catProms = value;
			},
			getConfirmation: function() {
				return confirmation;
			},
			setConfirmation: function(value) {
				confirmation = value;
			},
			getPrevLoc: function() {
				return prevLoc;
			},
			setPrevLoc: function(value) {
				prevLoc = value;
			},
			getSale: function() {
				return sale;
			},
			setSale: function(value) {
				sale = value;
			},
			getSelectedCat: function() {
				return selectedCat;
			},
			setSelectedCat: function(value) {
				selectedCat = value;
			},
			getSurcharge: function() {
				return surcharge;
			},
			setSurcharge: function(value) {
				surcharge = value;
			}
		};
	}).factory('Init', ['$resource',
	function($resource) {
		return $resource('/main?json=true', {}, {
			query: {method: 'GET', isObject: true}
		});
	}]).factory('Cart', ['$resource',
	function($resource) {
		return $resource('/viewCart?json=true', {}, {
			query: {method: 'GET', isObject: true}
		});
	}]);

