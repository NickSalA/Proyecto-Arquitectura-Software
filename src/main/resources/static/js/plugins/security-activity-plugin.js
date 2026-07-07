(function () {
    "use strict";

    var enabledSelector = "[data-security-activity]";
    var boundFlag = "saPluginBound";

    var defaults = {
        timeout: 60,
        warning: 10,
        heartbeat: 30
    };

    var state = {
        idleTime: 0,
        locked: false,
        warned: false,
        intervalId: null,
        heartbeatId: null
    };

    function init(root) {
        var scope = root || document;
        var el = scope.querySelector(enabledSelector);
        if (!el || el.dataset[boundFlag] === "true") return;
        el.dataset[boundFlag] = "true";

        var opts = readOptions(el);

        startTracking(el, opts);
        state.heartbeatId = setInterval(sendHeartbeat, opts.heartbeat * 1000);
        state.intervalId = setInterval(function () {
            tick(el, opts);
        }, 1000);

        sendHeartbeat();
    }

    function readOptions(el) {
        return {
            timeout: parseInt(el.getAttribute("data-sa-timeout")) || defaults.timeout,
            warning: parseInt(el.getAttribute("data-sa-warning")) || defaults.warning,
            heartbeat: parseInt(el.getAttribute("data-sa-heartbeat")) || defaults.heartbeat
        };
    }

    function startTracking(el, opts) {
        var reset = function () {
            state.idleTime = 0;
            if (state.warned) {
                hideWarning();
                state.warned = false;
            }
        };

        el.addEventListener("mousemove", reset);
        el.addEventListener("mousedown", reset);
        el.addEventListener("click", reset);
        el.addEventListener("keydown", reset);
        el.addEventListener("touchstart", reset);
        el.addEventListener("scroll", reset);
    }

    function tick(el, opts) {
        state.idleTime++;

        if (state.locked) return;

        var remaining = opts.timeout - state.idleTime;

        if (remaining <= 0) {
            lockScreen(el);
            return;
        }

        if (remaining <= opts.warning && !state.warned) {
            showWarning(remaining);
            state.warned = true;
        }

        if (remaining > opts.warning && state.warned) {
            hideWarning();
            state.warned = false;
        }
    }

    function showWarning(seconds) {
        var bar = document.getElementById("sa-warning-bar");
        if (!bar) {
            bar = document.createElement("div");
            bar.id = "sa-warning-bar";
            bar.className = "sa-warning-bar";
            bar.setAttribute("role", "alert");
            document.body.appendChild(bar);
        }
        bar.textContent = "Sesion inactiva. Se bloqueara en " + seconds + " segundo(s). Mueva el mouse.";
        bar.style.display = "block";
    }

    function hideWarning() {
        var bar = document.getElementById("sa-warning-bar");
        if (bar) bar.style.display = "none";
    }

    function lockScreen(el) {
        if (state.locked) return;
        state.locked = true;

        state.idleTime = 0;

        var overlay = document.getElementById("sa-lock-overlay");
        if (!overlay) {
            overlay = document.createElement("div");
            overlay.id = "sa-lock-overlay";
            overlay.className = "sa-lock-overlay";
            overlay.innerHTML =
                '<div class="sa-lock-card">' +
                '<div class="sa-lock-icon">&#128274;</div>' +
                '<h2>Sesion bloqueada por inactividad</h2>' +
                '<p>Mueva el mouse para desbloquear.</p>' +
                "</div>";
            document.body.appendChild(overlay);
        }
        overlay.style.display = "flex";

        sendEstado("inactivo");

        var unlock = function () {
            state.locked = false;
            state.idleTime = 0;
            overlay.style.display = "none";
            sendEstado("activo");
            el.removeEventListener("mousemove", unlock);
        };

        el.addEventListener("mousemove", unlock);
    }

    function sendHeartbeat() {
        sendEstado(state.locked ? "inactivo" : "activo");
    }

    function sendEstado(estado) {
        try {
            fetch("/api/actividad/heartbeat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ operador: "operador", estado: estado })
            }).catch(function () {});
        } catch (_) {}
    }

    window.MinimarketSecurityPlugin = {
        init: init,
        lockScreen: lockScreen,
        unlock: function () { state.locked = false; }
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", function () {
            init(document);
        });
    } else {
        init(document);
    }
})();
