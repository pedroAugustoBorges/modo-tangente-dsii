(() => {
    const form = document.getElementById("chat-form");
    const input = document.getElementById("chat-content");
    const status = document.getElementById("chat-status");
    const submit = form.querySelector("button[type=submit]");
    const history = document.getElementById("chat-history");
    const previewTemplate = document.getElementById("chat-message-preview");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
    let pending;

    history.addEventListener("click", (event) => {
        const button = event.target.closest("[data-retry-id]");
        if (!button || submit.disabled) return;

        const savedContent = button.dataset.retryContent;
        if (input.value.trim() && input.value !== savedContent) {
            showStatus("Envie ou limpe o rascunho antes de recuperar outra mensagem.");
            return;
        }
        input.value = savedContent;
        pending = { content: savedContent, requestId: button.dataset.retryId };
        showStatus("Mensagem recuperada. Use Enviar para consultar ou tentar novamente.");
        input.focus();
    });

    input.addEventListener("keydown", (event) => {
        if (event.key !== "Enter" || event.shiftKey || event.ctrlKey || event.altKey || event.metaKey
            || event.isComposing || event.keyCode === 229) {
            return;
        }
        event.preventDefault();
        if (!event.repeat && !submit.disabled) {
            form.requestSubmit(submit);
        }
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (submit.disabled) return;

        const content = input.value;
        if (!content.trim()) {
            showStatus("Digite uma mensagem.");
            return;
        }
        if (!pending || pending.content !== content) {
            pending = { content, requestId: newRequestId() };
        }

        input.focus({ preventScroll: true });
        const preview = showPreview();
        setBusy(true);
        showStatus("Aguardando resposta do tutor…", "waiting");
        try {
            const response = await fetch(form.dataset.endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json",
                    [form.dataset.csrfHeader]: form.dataset.csrfToken
                },
                body: JSON.stringify(pending)
            });

            if (response.redirected || response.status === 401 || response.status === 403) {
                showStatus("Sua sessão expirou. Copie o rascunho antes de entrar novamente.");
                return;
            }
            if (response.status === 202) {
                showStatus("A resposta ainda está pendente. Use Enviar para consultar novamente.");
                return;
            }
            if (!response.ok) {
                showStatus("Não foi possível obter a resposta. Use Enviar para tentar novamente.");
                return;
            }

            input.value = "";
            const previewRequestId = preview ? pending.requestId : null;
            pending = null;
            try {
                await refreshHistory(previewRequestId);
                showStatus("Resposta do tutor recebida.", "announced");
            } catch {
                window.location.reload();
            }
        } catch {
            showStatus("Falha de conexão. Seu texto foi preservado. Use Enviar para tentar novamente.");
        } finally {
            if (preview) {
                preview.remove();
                const empty = history.querySelector(".empty-state");
                if (empty) empty.hidden = false;
            }
            setBusy(false);
        }
    });

    function showPreview() {
        const savedMessage = history.querySelector('[data-message-id][data-request-id="' + pending.requestId + '"]');
        if (savedMessage) return null;

        const preview = previewTemplate.content.firstElementChild.cloneNode(true);
        preview.querySelector(".chat-message").textContent = pending.content;
        const empty = history.querySelector(".empty-state");
        if (empty) empty.hidden = true;
        history.querySelector(".chat-history").append(preview);
        return preview;
    }

    async function refreshHistory(previewRequestId) {
        const response = await fetch(form.dataset.historyUrl, { cache: "no-store" });
        if (!response.ok || response.redirected) {
            throw new Error("Histórico indisponível.");
        }
        const fragment = new DOMParser().parseFromString(await response.text(), "text/html");
        const fresh = fragment.getElementById("chat-history");
        if (!fresh) throw new Error("Histórico ausente.");

        const shown = new Set(Array.from(history.querySelectorAll("[data-message-id]"), message => message.dataset.messageId));
        let firstNewMessage;
        for (const message of fresh.querySelector(".chat-history").children) {
            if (shown.has(message.dataset.messageId) || message.dataset.requestId === previewRequestId) continue;
            message.classList.add("chat-entry-enter");
            firstNewMessage ??= message;
        }
        const followReply = form.getBoundingClientRect().top < window.innerHeight;
        history.replaceChildren(...fresh.childNodes);
        enableRetryButtons();
        if (followReply && firstNewMessage) {
            firstNewMessage.scrollIntoView({ block: "nearest", behavior: reducedMotion.matches ? "auto" : "smooth" });
        }
    }

    function showStatus(text, state = "") {
        status.textContent = text;
        status.dataset.state = state;
    }

    function enableRetryButtons() {
        for (const button of history.querySelectorAll("[data-retry-id]")) {
            button.hidden = false;
        }
    }

    function setBusy(busy) {
        submit.disabled = busy;
        input.readOnly = busy;
        form.setAttribute("aria-busy", String(busy));
        for (const button of history.querySelectorAll("[data-retry-id]")) button.disabled = busy;
    }

    function newRequestId() {
        const bytes = crypto.getRandomValues(new Uint8Array(16));
        bytes[6] = (bytes[6] & 15) | 64;
        bytes[8] = (bytes[8] & 63) | 128;
        const hex = Array.from(bytes, (byte) => byte.toString(16).padStart(2, "0")).join("");
        return [
            hex.slice(0, 8), hex.slice(8, 12), hex.slice(12, 16),
            hex.slice(16, 20), hex.slice(20)
        ].join("-");
    }

    enableRetryButtons();
    form.hidden = false;
})();
