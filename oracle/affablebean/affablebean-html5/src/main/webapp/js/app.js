'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', [
	'ngAnimate',
	'ngResource',
	'ngRoute',
	'myApp.controllers',
	'myApp.directives',
	'myApp.filters',
	'myApp.services',
	'myApp.animations'
]).
	config(['$routeProvider', function($routeProvider) {
			$routeProvider.when('/cart', {templateUrl: 'partials/cart.html',
				controller: 'cartCtrl'});
			$routeProvider.when('/category/:id', {templateUrl: 'partials/category.html',
				controller: 'categoryCtrl'});
			$routeProvider.when('/checkout', {templateUrl: 'partials/checkout.html',
				controller: 'checkoutCtrl'});
			$routeProvider.when('/confirmation', {templateUrl: 'partials/confirmation.html',
				controller: 'confirmationCtrl'});
			$routeProvider.when('/contact', {templateUrl: 'partials/contact.html',
				controller: 'contactCtrl'});
			$routeProvider.when('/main', {templateUrl: 'partials/main.html',
				controller: 'mainCtrl'});
			$routeProvider.when('/privacy', {templateUrl: 'partials/privacy.html'});
			$routeProvider.otherwise({redirectTo: '/main'});
		}]);
