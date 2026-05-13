// CSP-safe bindings for tasks app
(function () {
  function safeCall(fn, arg) {
    try {
      if (typeof fn === "function") fn(arg);
    } catch (e) {
      // swallow — runtime may not expose handlers in guest mode
    }
  }

  document.addEventListener(
    "click",
    function (e) {
      var btn = e.target.closest("[data-action]");
      if (btn) {
        var action = btn.getAttribute("data-action");
        var id = btn.getAttribute("data-id");
        switch (action) {
          case "copy-short-url":
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
          case "open-create-modal":
            safeCall(window.openCreateModal);
            return;
          case "close-modal":
            safeCall(window.closeModal);
            return;
          case "open-edit-modal":
            safeCall(function () {
              window.openEditModal && window.openEditModal(Number(id));
            });
            return;
          case "open-delete-modal":
            safeCall(function () {
              window.openDeleteModal && window.openDeleteModal(Number(id));
            });
            return;
          case "confirm-delete":
            safeCall(window.confirmDelete);
            return;
          case "view-task":
            safeCall(function () {
              window.viewTask && window.viewTask(Number(id));
            });
            return;
          case "close-delete-modal":
            document.getElementById("delete-modal")?.close();
            return;
          case "close-view-modal":
            document.getElementById("view-modal")?.close();
            return;
        }
      }

    },
    false,
  );

  // form submit delegation
  document.addEventListener(
    "submit",
    function (e) {
      var fm = e.target.closest("form[data-action='submit-task-form']");
      if (fm) {
        e.preventDefault();
        safeCall(function () {
          window.handleSubmit && window.handleSubmit(e);
        });
      }
    },
    false,
  );

  // drag events (delegated)
  document.addEventListener(
    "dragstart",
    function (e) {
      var card = e.target.closest(".task-card[data-id]");
      if (card) safeCall(window.handleDragStart, e);
    },
    false,
  );
  document.addEventListener(
    "dragend",
    function (e) {
      var card = e.target.closest(".task-card[data-id]");
      if (card) safeCall(window.handleDragEnd, e);
    },
    false,
  );

  // column drag/drop delegation
  document.addEventListener(
    "dragover",
    function (e) {
      var col = e.target.closest("[data-drag-column]");
      if (col) safeCall(window.handleDragOver, e);
    },
    false,
  );
  document.addEventListener(
    "dragleave",
    function (e) {
      var col = e.target.closest("[data-drag-column]");
      if (col) safeCall(window.handleDragLeave, e);
    },
    false,
  );
  document.addEventListener(
    "drop",
    function (e) {
      var col = e.target.closest("[data-drag-column]");
      if (col) safeCall(window.handleDrop, e);
    },
    false,
  );
})();
