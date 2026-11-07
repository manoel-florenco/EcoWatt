const state = {
    appliances: [],
    settings: null,
    summary: null,
    plan: null,
    mode: "POWER_TIME"
};

const presets = [
    { icon: "🧊", name: "Geladeira", powerWatts: 150, hoursPerDay: 8, daysPerMonth: 30 },
    { icon: "📺", name: "Televisão", powerWatts: 100, hoursPerDay: 5, daysPerMonth: 30 },
    { icon: "❄️", name: "Ar-condicionado", powerWatts: 1100, hoursPerDay: 6, daysPerMonth: 20 },
    { icon: "🚿", name: "Chuveiro elétrico", powerWatts: 5500, hoursPerDay: 0.4, daysPerMonth: 30 },
    { icon: "💡", name: "Lâmpada", powerWatts: 10, hoursPerDay: 5, daysPerMonth: 30 },
    { icon: "💻", name: "Notebook", powerWatts: 60, hoursPerDay: 6, daysPerMonth: 22 }
];

const colors = ["#247a3b", "#72a844", "#d5a42a", "#3e8d8a", "#8f63b8", "#cf6f42"];
const $ = (selector) => document.querySelector(selector);
const money = (value) => Number(value || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const number = (value, digits = 1) => Number(value || 0).toLocaleString("pt-BR", { maximumFractionDigits: digits });
const rate = (value) => Number(value || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 5 });
const escapeHtml = (value) => String(value ?? "").replace(/[&<>'"]/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;"
}[char]));

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: { "Content-Type": "application/json", ...(options.headers || {}) },
        ...options
    });
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        const details = body.fieldErrors ? Object.values(body.fieldErrors).join(" · ") : "";
        throw new Error(details || body.message || "Não foi possível concluir a operação.");
    }
    return response.status === 204 ? null : response.json();
}

let notificationTimer;
function notify(message, isError = false) {
    const element = $("#notification");
    element.textContent = message;
    element.classList.toggle("error", isError);
    element.hidden = false;
    clearTimeout(notificationTimer);
    notificationTimer = setTimeout(() => { element.hidden = true; }, 4200);
}

async function refresh() {
    try {
        const [settings, summary, plan] = await Promise.all([
            api("/api/settings"),
            api("/api/analysis/summary"),
            api("/api/analysis/savings-plan")
        ]);
        state.settings = settings;
        state.summary = summary;
        state.plan = plan;
        state.appliances = summary.ranking;
        renderAll();
    } catch (error) {
        notify("A API Java não respondeu: " + error.message, true);
    }
}

function renderAll() {
    renderKpis();
    renderRanking();
    renderChart();
    renderAppliances();
    renderSimulationOptions();
    renderSettings();
    renderPlan();
    renderReport();
    renderGoalCallout();
}

function renderKpis() {
    const summary = state.summary;
    const items = [
        ["Consumo mensal", `${number(summary.totalConsumptionKwh)} kWh`],
        ["Energia pela tarifa-base", money(summary.baseEnergyCost)],
        [`Adicional · ${state.settings.tariffFlagLabel || "Bandeira"}`, money(summary.flagAdditionalCost)],
        ["Total estimado", money(summary.totalMonthlyCost)],
        ["Aparelhos cadastrados", summary.applianceCount],
        ["Economia teórica", money(summary.theoreticalSavings)]
    ];
    $("#kpi-grid").innerHTML = items.map(([label, value]) => `
        <article class="kpi"><span>${label}</span><strong>${value}</strong></article>
    `).join("");
}

function renderRanking() {
    const container = $("#ranking");
    if (!state.appliances.length) {
        container.innerHTML = emptyState("🏠", "Nenhum aparelho cadastrado", "Adicione um aparelho ou use a residência de exemplo.");
        return;
    }
    const max = Math.max(...state.appliances.map(item => item.monthlyCost), 1);
    container.innerHTML = state.appliances.slice(0, 5).map((item, index) => `
        <div class="ranking-item">
            <span class="rank">${String(index + 1).padStart(2, "0")}</span>
            <div><strong>${escapeHtml(item.icon)} ${escapeHtml(item.name)}</strong><small>${number(item.consumptionKwh)} kWh/mês</small></div>
            <span class="ranking-item__value">${money(item.monthlyCost)}</span>
            <div class="bar" aria-hidden="true"><span style="width:${item.monthlyCost / max * 100}%"></span></div>
        </div>
    `).join("");
}

function renderChart() {
    const container = $("#consumption-chart");
    const total = state.summary.totalConsumptionKwh;
    if (!state.appliances.length || total <= 0) {
        container.innerHTML = emptyState("📊", "Ainda sem dados", "O gráfico será criado a partir dos consumos cadastrados.");
        return;
    }
    let cursor = 0;
    const segments = state.appliances.slice(0, 6).map((item, index) => {
        const percent = item.consumptionKwh / total * 100;
        const segment = `${colors[index % colors.length]} ${cursor}% ${cursor + percent}%`;
        cursor += percent;
        return segment;
    });
    if (cursor < 100) segments.push(`#dfe7df ${cursor}% 100%`);

    container.innerHTML = `
        <div class="donut-wrap">
            <div class="donut" style="background:conic-gradient(${segments.join(",")})">
                <div class="donut__label"><strong>${number(total)} kWh</strong><span>por mês</span></div>
            </div>
            <div class="legend">
                ${state.appliances.slice(0, 6).map((item, index) => `
                    <div style="--legend-color:${colors[index % colors.length]}">
                        <span>${escapeHtml(item.name)}</span>
                        <strong>${number(item.consumptionKwh / total * 100)}%</strong>
                    </div>
                `).join("")}
            </div>
        </div>
    `;
}

function renderAppliances() {
    $("#appliance-list-title").textContent = `Aparelhos cadastrados (${state.appliances.length})`;
    const container = $("#appliance-list");
    if (!state.appliances.length) {
        container.innerHTML = emptyState("🔌", "Sua lista está vazia", "O primeiro cadastro leva menos de um minuto.");
        return;
    }
    container.innerHTML = `<div class="appliance-list">${state.appliances.map(item => `
        <article class="appliance-card">
            <span class="appliance-card__icon" aria-hidden="true">${escapeHtml(item.icon)}</span>
            <div>
                <strong>${escapeHtml(item.name)}</strong>
                <small>${item.mode === "POWER_TIME"
                    ? `${number(item.powerWatts)} W · ${number(item.hoursPerDay)} h/dia · ${item.quantity} un.`
                    : `Consumo informado · ${item.quantity} un.`}</small>
                ${item.essential ? '<span class="tag">Essencial</span>' : ""}
            </div>
            <div class="appliance-card__numbers">
                <strong>${money(item.monthlyCost)}</strong>
                <small>${number(item.consumptionKwh)} kWh/mês</small>
                ${item.flagAdditionalCost > 0 ? `<small>+ ${money(item.flagAdditionalCost)} de bandeira</small>` : ""}
            </div>
            <div class="appliance-card__actions">
                <button class="mini-button" type="button" data-action="edit" data-id="${item.id}" aria-label="Editar ${escapeHtml(item.name)}">✏️</button>
                <button class="mini-button" type="button" data-action="delete" data-id="${item.id}" aria-label="Excluir ${escapeHtml(item.name)}">🗑️</button>
            </div>
        </article>
    `).join("")}</div>`;
}

function renderSimulationOptions() {
    const select = $("#simulation-appliance");
    const previous = select.value;
    if (!state.appliances.length) {
        select.innerHTML = '<option value="">Cadastre um aparelho primeiro</option>';
        select.disabled = true;
        $("#simulation-value").disabled = true;
        return;
    }
    select.disabled = false;
    $("#simulation-value").disabled = false;
    select.innerHTML = state.appliances.map(item => `
        <option value="${item.id}">${escapeHtml(item.icon)} ${escapeHtml(item.name)}</option>
    `).join("");
    if (state.appliances.some(item => String(item.id) === previous)) select.value = previous;
    updateSimulationInput();
}

function updateSimulationInput() {
    const item = state.appliances.find(candidate => String(candidate.id) === $("#simulation-appliance").value);
    if (!item) return;
    const isPower = item.mode === "POWER_TIME";
    const original = isPower ? item.hoursPerDay : item.monthlyKwh;
    $("#simulation-value-label").childNodes[0].textContent = isPower ? "Novas horas por dia " : "Novo consumo mensal (kWh) ";
    $("#simulation-value").max = isPower ? "24" : "";
    $("#simulation-value").value = original;
    $("#simulation-original").textContent = `Valor atual: ${number(original)} ${isPower ? "h/dia" : "kWh/mês"}.`;
}

function renderSettings() {
    if (!state.settings) return;
    $("#location").value = state.settings.location || "";
    $("#tariffPerKwh").value = state.settings.tariffPerKwh;
    $("#tariffFlag").value = state.settings.tariffFlag || "";
    $("#savingsGoal").value = state.settings.savingsGoal;
    $("#maxReductionPercent").value = state.settings.maxReductionPercent;

    $("#setup-location").value = state.settings.location || "";
    $("#setup-tariff").value = state.settings.tariffPerKwh || "";
    $("#setup-flag").value = state.settings.tariffFlag || "";
    $("#setup-overlay").hidden = state.settings.configured;
    document.body.classList.toggle("setup-open", !state.settings.configured);

    const context = $("#energy-context");
    $("#energy-context-text").textContent = `${state.settings.location || "Local não informado"} · ${state.settings.tariffFlagLabel || "Bandeira pendente"} · R$ ${rate(state.settings.effectiveTariffPerKwh)}/kWh`;
    context.hidden = !state.settings.configured;
    configureEnergyDialog(state.settings.configured);
    updateTariffPreview();
}

function configureEnergyDialog(editing) {
    $("#setup-eyebrow").textContent = editing ? "Configuração de energia" : "Antes de começar";
    $("#setup-title").textContent = editing ? "Atualize os dados da sua região" : "Configure a energia da sua região";
    $("#setup-description").textContent = editing
        ? "Altere a tarifa ou a bandeira vigente. Os totais serão recalculados imediatamente."
        : "Esses dados são obrigatórios porque a tarifa local e a bandeira vigente alteram diretamente todas as estimativas.";
    $("#setup-submit").textContent = editing ? "Salvar alterações" : "Salvar e iniciar simulação";
    $("#setup-close").hidden = !editing;
    $("#setup-cancel").hidden = !editing;
}

function openEnergySettings() {
    if (!state.settings?.configured) return;
    $("#setup-location").value = state.settings.location || "";
    $("#setup-tariff").value = state.settings.tariffPerKwh || "";
    $("#setup-flag").value = state.settings.tariffFlag || "";
    configureEnergyDialog(true);
    $("#setup-overlay").hidden = false;
    document.body.classList.add("setup-open");
    $("#setup-location").focus();
}

function closeEnergySettings() {
    if (!state.settings?.configured) return;
    $("#setup-overlay").hidden = true;
    document.body.classList.remove("setup-open");
    $("#energy-context").focus();
}

function updateTariffPreview() {
    const base = Number($("#tariffPerKwh").value || 0);
    const flag = $("#tariffFlag");
    const surcharge = { GREEN: 0, YELLOW: 0.01885, RED_LEVEL_1: 0.04463, RED_LEVEL_2: 0.07877 }[flag.value] || 0;
    const label = flag.selectedOptions[0]?.textContent.replace(/^[^A-Za-zÀ-ÿ]+/, "").split(" — ")[0] || "Bandeira";
    $("#tariff-preview").innerHTML = `<span>Tarifa efetiva</span><strong>R$ ${rate(base + surcharge)}/kWh</strong><small>R$ ${rate(base)} da tarifa-base + R$ ${rate(surcharge)} da ${escapeHtml(label.toLowerCase())}</small>`;
}

function renderPlan() {
    const container = $("#savings-plan");
    const plan = state.plan;
    if (!state.appliances.length) {
        container.innerHTML = emptyState("🎯", "Cadastre aparelhos", "A meta depende dos consumos da residência.");
        return;
    }
    container.innerHTML = `
        <span class="eyebrow">Resultado</span>
        <h2>${plan.achievable ? "Meta alcançável" : "Meta acima do cenário"}</h2>
        <div class="plan-summary">
            <span>${plan.achievable ? "Economia planejada" : "Economia máxima estimada"}</span>
            <strong>${money(plan.achievable ? plan.plannedSavings : plan.maximumPossibleSavings)}</strong>
            <small>${plan.achievable ? `${money(plan.plannedSavings * 12)} em um ano` : `Ainda faltariam ${money(plan.amountMissing)} por mês`}</small>
        </div>
        ${plan.actions.length ? `
            <h3>Sugestões</h3>
            ${plan.actions.map(action => `
                <div class="plan-action">
                    <span class="plan-action__icon">${escapeHtml(action.icon)}</span>
                    <div><strong>${escapeHtml(action.applianceName)}</strong><small>Reduzir de ${number(action.currentValue)} para ${number(action.suggestedValue)} ${action.mode === "POWER_TIME" ? "h/dia" : "kWh/mês"}</small></div>
                    <strong>${money(action.monthlySavings)}</strong>
                </div>
            `).join("")}
        ` : '<p class="hint">Não há aparelhos não essenciais com consumo para reduzir.</p>'}
    `;
}

function renderGoalCallout() {
    const plan = state.plan;
    if (!state.appliances.length) return;
    $("#goal-callout-title").textContent = plan.achievable
        ? `Sua meta de ${money(plan.goal)} é alcançável`
        : `A meta precisa de um ajuste`;
    $("#goal-callout-text").textContent = plan.achievable
        ? `${plan.actions.length} mudança(s) podem gerar ${money(plan.plannedSavings)} de economia mensal.`
        : `Com os limites atuais, a economia máxima estimada é ${money(plan.maximumPossibleSavings)} por mês.`;
}

function renderReport() {
    const report = $("#report-content");
    const summary = state.summary;
    const plan = state.plan;
    const date = new Date().toLocaleDateString("pt-BR", { day: "2-digit", month: "long", year: "numeric" });
    report.innerHTML = `
        <header class="report__header">
            <span class="eyebrow">Relatório de consumo residencial</span>
            <h1>EcoWatt</h1>
            <p>${escapeHtml(state.settings.location)} · gerado em ${date}</p>
            <p>Tarifa-base: R$ ${rate(state.settings.tariffPerKwh)}/kWh · ${escapeHtml(state.settings.tariffFlagLabel)}: + R$ ${rate(state.settings.flagSurchargePerKwh)}/kWh · tarifa efetiva: R$ ${rate(state.settings.effectiveTariffPerKwh)}/kWh</p>
        </header>
        <div class="kpi-grid">
            <div class="kpi"><span>Consumo mensal</span><strong>${number(summary.totalConsumptionKwh)} kWh</strong></div>
            <div class="kpi"><span>Tarifa-base</span><strong>${money(summary.baseEnergyCost)}</strong></div>
            <div class="kpi"><span>Adicional da bandeira</span><strong>${money(summary.flagAdditionalCost)}</strong></div>
            <div class="kpi"><span>Total estimado</span><strong>${money(summary.totalMonthlyCost)}</strong></div>
            <div class="kpi"><span>Economia planejada</span><strong>${money(plan.plannedSavings)}</strong></div>
        </div>
        <section class="report-section">
            <h2>Detalhamento dos aparelhos</h2>
            ${state.appliances.length ? `
                <div style="overflow-x:auto"><table class="report-table">
                    <thead><tr><th>Aparelho</th><th>Consumo</th><th>Base</th><th>Bandeira</th><th>Total</th></tr></thead>
                    <tbody>${state.appliances.map(item => `
                        <tr><td>${escapeHtml(item.icon)} ${escapeHtml(item.name)}</td><td>${number(item.consumptionKwh)} kWh</td><td>${money(item.baseMonthlyCost)}</td><td>${money(item.flagAdditionalCost)}</td><td>${money(item.monthlyCost)}</td></tr>
                    `).join("")}</tbody>
                </table></div>
            ` : "<p>Nenhum aparelho cadastrado.</p>"}
        </section>
        <section class="report-section">
            <h2>Conclusão</h2>
            <p>${plan.achievable
                ? `A meta de ${money(plan.goal)} por mês é matematicamente alcançável com as reduções sugeridas. Isso representa até ${money(plan.plannedSavings * 12)} em um ano.`
                : `A meta de ${money(plan.goal)} supera a economia estimada com o limite atual. O cenário calculado alcança ${money(plan.maximumPossibleSavings)} por mês.`}</p>
            <p class="hint">Os valores são estimativas lineares e já incluem a bandeira selecionada. Impostos, iluminação pública, potência real, ciclos automáticos e hábitos podem alterar o valor final da conta.</p>
        </section>
    `;
}

function emptyState(icon, title, text) {
    return `<div class="empty-state"><span>${icon}</span><h2>${title}</h2><p>${text}</p></div>`;
}

function setMode(mode) {
    state.mode = mode;
    document.querySelectorAll("[data-mode]").forEach(button => button.classList.toggle("active", button.dataset.mode === mode));
    $("#power-fields").hidden = mode !== "POWER_TIME";
    $("#kwh-fields").hidden = mode !== "MONTHLY_KWH";
}

function resetApplianceForm() {
    $("#appliance-form").reset();
    $("#appliance-id").value = "";
    $("#quantity").value = 1;
    $("#daysPerMonth").value = 30;
    $("#icon").value = "🔌";
    $("#form-title").textContent = "Novo aparelho";
    setMode("POWER_TIME");
}

function editAppliance(id) {
    const item = state.appliances.find(candidate => candidate.id === Number(id));
    if (!item) return;
    $("#appliance-id").value = item.id;
    $("#name").value = item.name;
    $("#icon").value = item.icon;
    $("#quantity").value = item.quantity;
    $("#powerWatts").value = item.powerWatts ?? "";
    $("#hoursPerDay").value = item.hoursPerDay ?? "";
    $("#daysPerMonth").value = item.daysPerMonth ?? 30;
    $("#monthlyKwh").value = item.monthlyKwh ?? "";
    $("#essential").checked = item.essential;
    $("#form-title").textContent = `Editar ${item.name}`;
    setMode(item.mode);
    location.hash = "appliances";
    $("#appliance-form").scrollIntoView({ behavior: "smooth", block: "start" });
}

async function saveAppliance(event) {
    event.preventDefault();
    const id = $("#appliance-id").value;
    const body = {
        name: $("#name").value.trim(),
        icon: $("#icon").value.trim() || "🔌",
        mode: state.mode,
        quantity: Number($("#quantity").value),
        powerWatts: state.mode === "POWER_TIME" ? Number($("#powerWatts").value) : null,
        hoursPerDay: state.mode === "POWER_TIME" ? Number($("#hoursPerDay").value) : null,
        daysPerMonth: state.mode === "POWER_TIME" ? Number($("#daysPerMonth").value) : null,
        monthlyKwh: state.mode === "MONTHLY_KWH" ? Number($("#monthlyKwh").value) : null,
        essential: $("#essential").checked
    };
    try {
        await api(id ? `/api/appliances/${id}` : "/api/appliances", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(body)
        });
        resetApplianceForm();
        await refresh();
        notify(id ? "Aparelho atualizado." : "Aparelho cadastrado.");
    } catch (error) {
        notify(error.message, true);
    }
}

async function saveSettings(event) {
    event.preventDefault();
    try {
        await api("/api/settings", {
            method: "PUT",
            body: JSON.stringify({
                location: $("#location").value.trim(),
                tariffPerKwh: Number($("#tariffPerKwh").value),
                tariffFlag: $("#tariffFlag").value,
                savingsGoal: Number($("#savingsGoal").value),
                maxReductionPercent: Number($("#maxReductionPercent").value)
            })
        });
        await refresh();
        notify("Parâmetros atualizados.");
    } catch (error) {
        notify(error.message, true);
    }
}

async function saveInitialSetup(event) {
    event.preventDefault();
    const wasConfigured = Boolean(state.settings?.configured);
    try {
        await api("/api/settings", {
            method: "PUT",
            body: JSON.stringify({
                location: $("#setup-location").value.trim(),
                tariffPerKwh: Number($("#setup-tariff").value),
                tariffFlag: $("#setup-flag").value,
                savingsGoal: Number(state.settings?.savingsGoal ?? 50),
                maxReductionPercent: Number(state.settings?.maxReductionPercent ?? 30)
            })
        });
        await refresh();
        notify(wasConfigured ? "Configuração de energia atualizada." : "Região, tarifa e bandeira configuradas.");
    } catch (error) {
        notify(error.message, true);
    }
}

async function simulate(event) {
    event.preventDefault();
    const applianceId = Number($("#simulation-appliance").value);
    if (!applianceId) {
        notify("Cadastre um aparelho antes de simular.", true);
        return;
    }
    try {
        const result = await api("/api/analysis/simulation", {
            method: "POST",
            body: JSON.stringify({ applianceId, simulatedValue: Number($("#simulation-value").value) })
        });
        const positive = result.monthlySavings >= 0;
        $("#simulation-result").innerHTML = `
            <span class="eyebrow">Resultado para ${escapeHtml(result.applianceName)}</span>
            <h2>${positive ? "Economia estimada" : "Aumento estimado"}</h2>
            <div class="result-highlight ${positive ? "" : "negative"}">
                <span>Por mês</span>
                <strong>${money(Math.abs(result.monthlySavings))}</strong>
                <small>${money(Math.abs(result.annualSavings))} em 12 meses · bandeira incluída</small>
            </div>
            <div class="metric-list">
                <div><span>Consumo original</span><strong>${number(result.originalConsumptionKwh)} kWh</strong></div>
                <div><span>Novo consumo</span><strong>${number(result.simulatedConsumptionKwh)} kWh</strong></div>
                <div><span>Valor original</span><strong>${number(result.originalValue)} ${result.mode === "POWER_TIME" ? "h/dia" : "kWh"}</strong></div>
                <div><span>Variação</span><strong>${number(Math.abs(result.reductionPercent))}%</strong></div>
            </div>
        `;
    } catch (error) {
        notify(error.message, true);
    }
}

function showView() {
    const requested = location.hash.slice(1);
    const view = document.querySelector(`[data-view="${requested}"]`) ? requested : "overview";
    document.querySelectorAll("[data-view]").forEach(section => {
        const active = section.dataset.view === view;
        section.hidden = !active;
        section.classList.toggle("active", active);
    });
    document.querySelectorAll("[data-view-link]").forEach(link => link.classList.toggle("active", link.dataset.viewLink === view));
    $("#main-nav").classList.remove("open");
    $("#menu-button").setAttribute("aria-expanded", "false");
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function initializePresets() {
    $("#preset-list").innerHTML = presets.map((preset, index) => `
        <button class="preset" type="button" data-action="preset" data-index="${index}">${preset.icon} ${preset.name}</button>
    `).join("");
}

document.addEventListener("click", async event => {
    const modeButton = event.target.closest("[data-mode]");
    if (modeButton) setMode(modeButton.dataset.mode);

    const button = event.target.closest("[data-action]");
    if (!button) return;
    const { action, id, index } = button.dataset;

    if (action === "preset") {
        const preset = presets[Number(index)];
        setMode("POWER_TIME");
        Object.entries(preset).forEach(([key, value]) => { const field = $(`#${key}`); if (field) field.value = value; });
    } else if (action === "new-appliance") {
        resetApplianceForm();
        $("#appliance-form").scrollIntoView({ behavior: "smooth" });
    } else if (action === "cancel-edit") {
        resetApplianceForm();
    } else if (action === "open-energy-settings") {
        openEnergySettings();
    } else if (action === "close-energy-settings") {
        closeEnergySettings();
    } else if (action === "edit") {
        editAppliance(id);
    } else if (action === "delete") {
        const item = state.appliances.find(candidate => candidate.id === Number(id));
        if (item && confirm(`Excluir "${item.name}"?`)) {
            try {
                await api(`/api/appliances/${id}`, { method: "DELETE" });
                await refresh();
                notify("Aparelho excluído.");
            } catch (error) { notify(error.message, true); }
        }
    } else if (action === "delete-all") {
        if (state.appliances.length && confirm("Excluir todos os aparelhos cadastrados?")) {
            try {
                await api("/api/appliances", { method: "DELETE" });
                await refresh();
                notify("Todos os aparelhos foram excluídos.");
            } catch (error) { notify(error.message, true); }
        }
    } else if (action === "load-demo") {
        if (!state.appliances.length || confirm("Substituir os aparelhos atuais pela residência de exemplo?")) {
            try {
                await api("/api/appliances/demo", { method: "POST" });
                await refresh();
                notify("Residência de exemplo carregada.");
            } catch (error) { notify(error.message, true); }
        }
    } else if (action === "print") {
        window.print();
    }
});

$("#menu-button").addEventListener("click", () => {
    const nav = $("#main-nav");
    const open = nav.classList.toggle("open");
    $("#menu-button").setAttribute("aria-expanded", String(open));
});
$("#appliance-form").addEventListener("submit", saveAppliance);
$("#setup-form").addEventListener("submit", saveInitialSetup);
$("#settings-form").addEventListener("submit", saveSettings);
$("#simulation-form").addEventListener("submit", simulate);
$("#simulation-appliance").addEventListener("change", updateSimulationInput);
$("#tariffPerKwh").addEventListener("input", updateTariffPreview);
$("#tariffFlag").addEventListener("change", updateTariffPreview);
window.addEventListener("hashchange", showView);
document.addEventListener("keydown", event => {
    if (event.key === "Escape" && !$("#setup-overlay").hidden) closeEnergySettings();
});

initializePresets();
resetApplianceForm();
showView();
refresh();
