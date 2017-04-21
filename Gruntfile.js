
module.exports = function(grunt) { 
	
	// Project configuration.
	grunt.initConfig({
	  pkg: grunt.file.readJSON('package.json'),
	  
	  jshint: {
          all: ['Gruntfile.js', './src/main/resources/static/js/*.js']
      },
      
      watch: {
    	  files: ['<%= jshint.files %>'],
          tasks: ['jshint']
      },
	  
      includeSource: {
		  options: {
		    basePath: 'src/main/resources/static',
		    baseUrl: '',
		    templates: {
		      html: {
		        js: '<script src="{filePath}"></script>',
		        css: '<link rel="stylesheet" type="text/css" href="{filePath}" />',
		      },
		      scss: {
		        scss: '@import "{filePath}";',
		        css: '@import "{filePath}";',
		      },
		      less: {
		        less: '@import "{filePath}";',
		        css: '@import "{filePath}";',
		      }
		    }
		  },
		  myTarget: {
		    files: {
		      './src/main/resources/static/index.html': './src/main/resources/templates/index.tpl.html'
		    }
		  }
		}
	  
	  
	});
	
	// These plugins provide necessary tasks.
	grunt.loadNpmTasks('grunt-contrib-jshint');
	grunt.loadNpmTasks('grunt-include-source');
	grunt.registerTask('default', ['jshint', 'includeSource']);
};