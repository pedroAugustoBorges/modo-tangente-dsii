(() => {
    const form = document.getElementById("chat-form");
    const input = document.getElementById("chat-content");
    const status = document.getElementById("chat-status");
    const submit = form.querySelector("button[type=submit]");
    const retryButtons = Array.from(document.querySelectorAll("[data-retry-id]"));
    let pending;

    for (const button of retryButtons) {
        const savedContent = button.closest("li").querySelector(".chat-message").textContent;
        button.hidden = false;
        button.addEventListener("click", () => {
            if (input.value.trim() && input.value !== savedContent) {
                status.textContent = "Envie ou limpe o rascunho antes de recuperar outra mensagem.";
                return;
            }
            input.value = savedContent;
            pending = { content: savedContent, requestId: button.dataset.retryId };
            status.textContent = "Mensagem recuperada. Use Enviar para consultar ou tentar novamente.";
            input.focus();
        });
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (submit.disabled) return;

        const content = input.value;
        if (!content.trim()) {
            status.textContent = "Digite uma mensagem.";
            return;
        }
        if (!pending || pending.content !== content) {
            pending = { content, requestId: newRequestId() };
        }

        setBusy(true);
        status.textContent = "Aguardando resposta do tutor…";
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
                status.textContent = "Sua sessão expirou. Copie o rascunho antes de entrar novamente.";
                return;
            }
            if (response.status === 202) {
                status.textContent = "A resposta ainda está pendente. Use Enviar para consultar novamente.";
                return;
            }
            if (!response.ok) {
                status.textContent = "Não foi possível obter a resposta. Use Enviar para tentar novamente.";
                return;
            }

            // Reload the canonical history so retries never append duplicate responses.
            input.value = "";
            window.location.reload();
        } catch {
            status.textContent = "Falha de conexão. Seu texto foi preservado. Use Enviar para tentar novamente.";
        } finally {
            setBusy(false);
        }
    });

    function setBusy(busy) {
        submit.disabled = busy;
        input.readOnly = busy;
        form.setAttribute("aria-busy", String(busy));
        for (const button of retryButtons) button.disabled = busy;
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

    form.hidden = false;
})();
