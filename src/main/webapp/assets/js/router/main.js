define(["backbone", "model/state"], function(Backbone, state) {

  //main and alone...
  var MainRouter = Backbone.Router.extend({

    initialize: function() {
      this.on("all", function(eventName) {
        this.previousRouteEvent = eventName;
      }, this);

      state.on("login", function(user) {
        this.previousRouteEvent === 'route:root' ? 
          this.navigate(user.get("login"), {trigger: true}) : 
          Backbone.history.loadUrl(Backbone.history.fragment);
      }, this);
    },

    routes: {
      "" :                 "root",
      ":userName" :        "userPage"
    },

    root: function() {
      state.get("mainView").clean().showWiki();
    },

    userPage: function() {
      state.get("mainView").clean().showUserPage();
    }

  });
  return MainRouter;
});