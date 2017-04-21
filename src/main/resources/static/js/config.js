var app = angular.module('app', ['ui.router', 'ngCookies']);


app.factory('authInterceptor', function($q, $location, $rootScope) {
	  return {
		  'responseError' : function(response) {
			  if (response.status == 401) {
				  return $location.path('/login');
			  }
			  return $q.reject(response);
		  }
	  };
});

app.config(function($stateProvider, $httpProvider, $locationProvider) {

	$stateProvider
	.state('login', {
    	url : '/login',
    	templateUrl : './html/login.html',
    	controller : 'loginCtrl',
    	controllerAs: 'login'
    })
	.state('resource', {
		url : '/secure/resource',
		templateUrl : './html/resource.html',
		controller : 'resourceCtrl',
		controllerAs: 'resource'
    })
    .state('logout', {
		url : '/logout',
		templateUrl : './html/logout.html'
    })
    .state('/', {
    	url : '*path',
    	template : 'Click on login to continue...'
    });    

	$httpProvider.interceptors.push('authInterceptor');
    $locationProvider.html5Mode(true);
  });
  
  