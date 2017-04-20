app.controller('loginCtrl', function($rootScope, $http, $state, $cookies) {

    var self = this;

    var authenticate = function(credentials, callback) {
	var config = {
		  headers: {'Content-Type': 'application/json'}
	};
      
    		          
	var creds = {
		  username : credentials.username,
		  password : credentials.password
	};
      
	$http.post('authenticate', angular.toJson(creds), config)
		.then(function(response) {
		if (response.status == '200') {
	          $rootScope.authenticated = true;
	        } else {
	          $rootScope.authenticated = false;
	        }
	        callback && callback();
	 	}, function() {
	        $rootScope.authenticated = false;
	        callback && callback();
	    });
	}		

    self.login = function() {
        authenticate(self.credentials, function() {
          if ($rootScope.authenticated) {
            $state.go('resource');
            self.error = false;
          } else {
        	$state.go('login');
            self.error = true;
          }
        });
    };
    
    self.logout = function() {
	  $http.post('logout', {}).finally(function() {
	    $rootScope.authenticated = false;
	    $state.go('logout');
	  });
	}
     
  })