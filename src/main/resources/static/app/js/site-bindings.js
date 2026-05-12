// CSP-safe bindings for tasks app
(function () {
  document.addEventListener(
    "click",
    function (e) {
      var btn = e.target.closest("[data-action]");
      if (btn) {
        var action = btn.getAttribute("data-action");
        if (action === "copy-short-url") {
          e.preventDefault();
          var payload = btn.getAttribute("data-copy");
          navigator.clipboard
            ?.writeText(payload)
            .then(function () {
              var orig = btn.textContent;
              btn.textContent = "COPIED!";
              setTimeout(function () {
                btn.textContent = orig;
              }, 1500);
            })
            .catch(() => {});
          return;
        }
      }

      var toggle = e.target.closest("#shell-mobile-toggle");
      if (toggle) {
        var panel = document.querySelector("[data-shell-mobile-panel]");
        var drawer = document.querySelector("[data-shell-mobile-drawer]");
        var backdrop = document.querySelector("[data-shell-mobile-backdrop]");
        if (panel && drawer) {
          var isOpen = !panel.classList.contains("hidden");
          if (isOpen) {
            panel.classList.add("hidden");
            drawer.classList.add("-translate-x-full");
          } else {
            panel.classList.remove("hidden");
            drawer.classList.remove("-translate-x-full");
          }
          if (backdrop) backdrop.classList.toggle("opacity-0");
        }
      }
    },
    false,
  );
})();
