define(["jquery", "underscore", "backbone", "text!template/auth-form-not-loggedin.js", "model/state", "luna", "bootstrap", "lib/serializeObject.jquery"], 
  function($, _, Backbone, tplAuthNotLoggedIn, state, Luna) {
  var TplAuthFormNotLoggedIn = _.template(tplAuthNotLoggedIn);


  var authForm = Backbone.View.extend({
    el: "#authorization-form",

    initialize: function() {
      this.render();
    },

    render: function() {
      var self = this;
      if(state.isLoggedIn()) {
        //show logout there
      } else {
        this.$el.append(TplAuthFormNotLoggedIn());//show login here
        this.$el.find(".dropdown-toggle").dropdown();
        this.$el.find(".auth-dropdown-form .login_btn").on("click", function(evt) {
          Luna.authorize(self.$el.find(".auth-dropdown-form").serializeObject(), function(data) {
            console.log(data);
          });
          return false;
        });
      }
    },

    clean: function() {
      this.$el.empty();
    }
  });
  return authForm;
});