app.controller('resourceCtrl', function($http, $cookies) {
	  var self = this;
	  $http.get('/secure/resource').then(function(response) {
		    self.greeting = response.data;
	  });
//	  var config = {
//		headers: {'Authorization': 'Bearer ' + $cookies.get('authToken')}
//	  };
//	  $cookies.remove("authToken");
//	  self.getProtectedResource = function() {
//		  console.log('get protected resource function');
//		  $http.get('/resource').then(function(response) {
//			    self.greeting = response.data;
//		  });
//	  };
  });