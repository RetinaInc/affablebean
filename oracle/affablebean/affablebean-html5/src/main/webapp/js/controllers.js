'use strict';

/* Controllers */

angular.module('myApp.controllers', []).
	controller('cartCtrl', ['$scope', '$http', '$routeParams', 'global',
		function($scope, $http, $routeParams, global) {
			var url = "/AffableBean/viewCart?json=true";

			if ($routeParams.clear == "true") {
				url += "&clear=true";
			}

			$http.get(url)
				.success(function(data) {
					updateCartView(data);
				});

			if ($routeParams.clear == "true") {
				window.location.href = '#/main';
				location.reload();
			}

			$scope.selectedCategory = global.getSelectedCat();

			$scope.updateCart = function(product, quantity) {
				$http({
					method: 'POST',
					url: '/AffableBean/updateCart?productId=' + product + '&quantity='
						+ quantity + '&json=true'

				}).success(function(data) {
					updateCartView(data);
					location.reload();
				});
			};

			var updateCartView = function(data) {
				$scope.cart = data['properties'];
				$scope.cartItems = data['cartItems'];
			};
		}]).
	controller('categoryCtrl', ['$scope', '$http', '$routeParams', 'global',
		function($scope, $http, $routeParams, global) {
			$http.get('/AffableBean/category?id=' + $routeParams.id + '&json=true')
				.success(function(data) {
					$scope.catProms = data['categoryPromotions'];
					$scope.prodProms = data['productPromotions'];
					$scope.products = data['products'];
					$scope.selectedCategory = $routeParams.id;
					global.setSelectedCat($routeParams.id);
				});

			$scope.categories = global.getCategories();
			$scope.sale = global.getSale();
			$scope.new_price = 0;

			$scope.setPrice = function(price) {
				$scope.new_price = price;
			};

			$scope.addtoCart = function(product) {
				$http({
					method: 'POST',
					url: '/AffableBean/addToCart?productId=' + product + '&json=true'

				}).success(function(data) {
					window.location.href = '#/category/' + $scope.selectedCategory;
					location.reload();
				});
			};
		}]).
	controller('checkoutCtrl', ['$scope', '$http', 'global', 'Cart',
		function($scope, $http, global, Cart) {
			Cart.get(function(data) {
				$scope.cart = data['properties'];
			});

			$scope.surcharge = global.getSurcharge();

			$scope.total = function() {
				return Number($scope.surcharge) + Number($scope.cart.subtotal);
			}

			$scope.customer = {};

			$scope.purchase = function() {
				$http({
					method: 'POST',
					url: '/AffableBean/purchase?json=true',
					data: $scope.customer

				}).success(function(data) {
					if (data !== null) {
						global.setConfirmation(data);
						window.location.href = '#/confirmation';
					}
				});
			};
		}]).
	controller('confirmationCtrl', ['$scope', 'global',
		function($scope, global) {
			$scope.confirmation = global.getConfirmation();
			$scope.customer = $scope.confirmation['customer'];
			$scope.order = $scope.confirmation['customerOrder'];
			$scope.orderedProducts = $scope.confirmation['orderedProducts'];
			$scope.surcharge = global.getSurcharge();
			global.setPrevLoc("confirmation");
		}]).
	controller('contactCtrl', ['$scope', '$http', function($scope, $http) {
			$http.get('/AffableBean/contact?json=true')
				.success(function(data) {
					$scope.subjects = data;
				});

			$scope.msg = {};

			$scope.sendMsg = function() {
				$http({
					method: 'POST',
					url: '/AffableBean/feedback?json=true',
					data: $scope.msg

				}).success(function(data) {
					$scope.errors = data;
					window.location.href = '#/main';
				});
			};
		}]).
	controller('indexCtrl', ['$scope', '$http', 'global', '$location', 'Init',
		'Cart',
		function($scope, $http, global, $location, Init, Cart) {
			Init.get(function(data) {
				$scope.sale = data['salePromotion'][0];
				global.setCategories(data['categories']);
				global.setSale($scope.sale);
				global.setSurcharge(data['properties']['surcharge']);
			});

			Cart.get(function(data) {
				$scope.cart = data['properties'];
			});

			$scope.location = $location;
		}]).
	controller('mainCtrl', ['$scope', 'global',
		function($scope, global) {
			$scope.categories = global.getCategories();

			if (global.getPrevLoc() == "confirmation") { // force reload to clear cart
				location.reload();
				global.setPrevLoc("");
			}
		}]);
		