(() => {
    const gsap = window.gsap;
    const screen = document.querySelector("[data-study-screen]");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
    const parts = screen ? Array.from(screen.querySelectorAll("[data-study-part]")) : [];
    const partsToAnimate = parts.length ? parts : [screen];
    const hintKey = "modo-tangente:study-navigation";
    const transitions = {
        forward: { enter: { x: 42 }, exit: { x: -48 }, staggerFrom: "end" },
        back: { enter: { x: -42 }, exit: { x: 48 }, staggerFrom: "start" },
        "chat-open": { enter: { y: 54 }, exit: { y: -20 } },
        "chat-close": { enter: { y: -28 }, exit: { y: 54 } }
    };
    const nativeSubmissions = new WeakSet();
    let screenAnimation;
    let answerAnimation;
    let leaving = false;

    function canAnimate() {
        return Boolean(gsap && screen && !reducedMotion.matches);
    }

    function recallDirection() {
        try {
            const hint = JSON.parse(sessionStorage.getItem(hintKey));
            sessionStorage.removeItem(hintKey);
            if (hint && Date.now() - hint.at < 10000
                && (Object.hasOwn(transitions, hint.direction) || hint.direction === "answer")) {
                return hint.direction;
            }
        } catch {
            // Navigation still works when storage is unavailable.
        }
        return null;
    }

    function rememberDirection(direction) {
        try {
            sessionStorage.setItem(hintKey, JSON.stringify({ direction, at: Date.now() }));
        } catch {
            // The destination can still infer a direction.
        }
    }

    function targets(direction) {
        return direction.startsWith("chat-") ? [screen] : partsToAnimate;
    }

    function inferredDirection(restored) {
        if (screen.dataset.studyScreen === "chat") return "chat-open";

        const activation = window.navigation?.activation;
        const from = activation?.from?.url ?? document.referrer;
        if (from) {
            const previous = new URL(from);
            if (previous.origin === location.origin && previous.pathname.endsWith("/chat")) {
                return "chat-close";
            }
        }
        if (activation?.navigationType === "traverse" && activation.from?.index >= 0) {
            return activation.entry.index < activation.from.index ? "back" : "forward";
        }
        const load = performance.getEntriesByType("navigation")[0];
        return restored || load?.type === "back_forward" ? "back" : "forward";
    }

    function enterScreen(restored = false) {
        const hint = recallDirection();
        if (!canAnimate()) return;

        if (hint !== "answer") {
            const direction = hint ?? inferredDirection(restored);
            screenAnimation = gsap.fromTo(targets(direction),
                { opacity: 0, ...transitions[direction].enter },
                {
                    opacity: 1, x: 0, y: 0, duration: 0.34, ease: "power3.out",
                    stagger: { amount: 0.13, from: transitions[direction].staggerFrom ?? "start" },
                    clearProps: "opacity,transform"
                });
        }

        const mark = !restored && screen.querySelector("[data-answer-celebration]");
        if (mark) {
            answerAnimation = gsap.timeline();
            answerAnimation.from(mark.closest(".option-correct"), {
                backgroundColor: "#f0edfe", borderColor: "#b9afff",
                boxShadow: "0 0 0 5px rgba(55, 166, 108, 0.18)",
                duration: 0.45, ease: "power2.out", clearProps: "backgroundColor,borderColor,boxShadow"
            }, 0);
            answerAnimation.from(mark, {
                opacity: 0, scale: 0.4, rotation: -20, duration: 0.44,
                ease: "back.out(2.5)", clearProps: "opacity,transform"
            }, 0.06);
        }
    }

    function leaveScreen(direction, navigate) {
        if (leaving) return;

        leaving = true;
        screenAnimation?.kill();
        screenAnimation = gsap.to(targets(direction), {
            opacity: 0, ...transitions[direction].exit,
            duration: 0.21, ease: "power2.in",
            stagger: { amount: 0.09, from: transitions[direction].staggerFrom ?? "start" },
            onComplete: () => {
                rememberDirection(direction);
                navigate();
            }
        });
    }

    document.addEventListener("click", (event) => {
        if (!canAnimate() || event.defaultPrevented || event.button !== 0
            || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;

        const link = event.target.closest("a[data-study-nav]");
        if (!link || (link.target && link.target !== "_self") || link.hasAttribute("download")) return;

        const destination = new URL(link.href);
        if (destination.origin !== location.origin
            || (destination.pathname === location.pathname && destination.search === location.search)) return;

        event.preventDefault();
        leaveScreen(link.dataset.studyNav, () => location.assign(destination.href));
    });

    document.addEventListener("submit", (event) => {
        const form = event.target;
        if (nativeSubmissions.delete(form)) return;
        if (!canAnimate() || event.defaultPrevented || !form.dataset.studyNav || !form.requestSubmit) return;

        event.preventDefault();
        leaveScreen(form.dataset.studyNav, () => {
            nativeSubmissions.add(form);
            if (event.submitter) form.requestSubmit(event.submitter);
            else form.requestSubmit();
        });
    });

    function skipNativeTransition(event) {
        const transition = event.viewTransition;
        if (!transition || !canAnimate()) return;

        // Skipping the native animation rejects its readiness promise.
        transition.ready.catch(() => {});
        transition.skipTransition();
    }

    window.addEventListener("pageswap", skipNativeTransition);
    window.addEventListener("pagereveal", (event) => {
        skipNativeTransition(event);
        document.documentElement.toggleAttribute("data-native-transition", Boolean(event.viewTransition && !canAnimate()));
    });

    const answerForm = document.querySelector("[data-question-answer]");
    if (answerForm) {
        const submit = answerForm.querySelector("button[type=submit]");
        const label = submit.textContent;
        answerForm.addEventListener("submit", (event) => {
            if (submit.disabled) {
                event.preventDefault();
                return;
            }
            if (canAnimate()) rememberDirection("answer");
            submit.disabled = true;
            submit.textContent = "Registrando…";
            answerForm.setAttribute("aria-busy", "true");
        });
        window.addEventListener("pageshow", (event) => {
            if (event.persisted) {
                submit.disabled = false;
                submit.textContent = label;
                answerForm.removeAttribute("aria-busy");
            }
        });
    }

    if (screen && gsap) {
        document.documentElement.toggleAttribute("data-gsap-motion", canAnimate());
        reducedMotion.addEventListener("change", () => {
            document.documentElement.toggleAttribute("data-gsap-motion", canAnimate());
            if (reducedMotion.matches) {
                screenAnimation?.progress(1);
                answerAnimation?.progress(1);
            }
        });
        window.addEventListener("pageshow", (event) => {
            if (!event.persisted) return;

            leaving = false;
            screenAnimation?.kill();
            gsap.set([screen, ...parts], { clearProps: "opacity,transform" });
            enterScreen(true);
        });
    }
    enterScreen();
})();
