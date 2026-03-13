const STORAGE_KEY = 'meterReadings';
const THEME_KEY = 'darkTheme';
let readings = [];
let chartInstance = null;

function loadReadings() {
    const stored = localStorage.getItem(STORAGE_KEY);
    try { readings = stored ? JSON.parse(stored) : []; } catch { readings = []; }
    renderTable();
    updateDiffInfo();
    updateChart();
}
function saveReadings() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(readings));
    renderTable();
    updateDiffInfo();
    updateChart();
}
function loadTheme() {
    const dark = localStorage.getItem(THEME_KEY) === 'true';
    document.body.classList.toggle('dark-theme', dark);
    document.getElementById('themeToggle').innerHTML = dark ? '<i class="fas fa-sun"></i> Светлая тема' : '<i class="fas fa-moon"></i> Тёмная тема';
}
function toggleTheme() {
    const isDark = document.body.classList.toggle('dark-theme');
    localStorage.setItem(THEME_KEY, isDark);
    document.getElementById('themeToggle').innerHTML = isDark ? '<i class="fas fa-sun"></i> Светлая тема' : '<i class="fas fa-moon"></i> Тёмная тема';
}
function addReading(type, value, date) {
    readings.push({ id: Date.now(), type, value: parseFloat(value), date });
    saveReadings();
}
function deleteReading(id) {
    readings = readings.filter(r => r.id !== id);
    saveReadings();
}
function clearAllReadings() {
    if (confirm('Удалить все записи?')) { readings = []; saveReadings(); }
}
function getTypeInfo(type) {
    const map = {
        cold: { text: '❄️ Холодная вода', icon: 'fa-solid fa-droplet' },
        hot: { text: '🔥 Горячая вода', icon: 'fa-solid fa-fire' },
        electric: { text: '⚡ Электричество', icon: 'fa-solid fa-bolt' },
        gas: { text: '⛽ Газ', icon: 'fa-solid fa-gas-pump' },
        heating: { text: '🌡️ Отопление', icon: 'fa-solid fa-temperature-high' }
    };
    return map[type] || { text: type, icon: 'fa-solid fa-question' };
}
function renderTable() {
    const tbody = document.getElementById('tableBody');
    if (!tbody) return;
    if (!readings.length) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;">Нет данных</td></tr>';
        return;
    }
    const sorted = [...readings].sort((a,b)=>new Date(b.date)-new Date(a.date));
    let html = '';
    sorted.forEach(r => {
        const info = getTypeInfo(r.type);
        html += `<tr><td><i class="${info.icon}"></i> ${info.text}</td><td>${r.value.toFixed(2)}</td><td>${r.date}</td><td><button class="delete-btn" data-id="${r.id}"><i class="fas fa-trash"></i> Удалить</button></td></tr>`;
    });
    tbody.innerHTML = html;
    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', e => deleteReading(Number(e.currentTarget.dataset.id)));
    });
}
function updateDiffInfo() {
    const container = document.getElementById('diffContainer');
    if (!container) return;
    const types = ['cold','hot','electric','gas','heating'];
    let html = '';
    types.forEach(type => {
        const info = getTypeInfo(type);
        const filtered = readings.filter(r=>r.type===type).sort((a,b)=>new Date(a.date)-new Date(b.date));
        let diff = null, note = 'Недостаточно данных';
        if (filtered.length>=2) {
            const last = filtered[filtered.length-1].value;
            const prev = filtered[filtered.length-2].value;
            diff = (last-prev).toFixed(2);
            note = `Последнее: ${last.toFixed(2)}, предпоследнее: ${prev.toFixed(2)}`;
        } else if (filtered.length===1) note = 'Только одно показание';
        html += `<div class="diff-item"><i class="${info.icon}"></i><div class="type">${info.text}</div><div class="value">${diff!==null?diff:'—'}</div><div class="note">${note}</div></div>`;
    });
    container.innerHTML = html;
}
function updateChart() {
    const ctx = document.getElementById('meterChart').getContext('2d');
    const type = document.getElementById('chartTypeSelect').value;
    const info = getTypeInfo(type);
    const filtered = readings.filter(r=>r.type===type).sort((a,b)=>new Date(a.date)-new Date(b.date));
    if (chartInstance) chartInstance.destroy();
    chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: filtered.map(r=>r.date),
            datasets: [{
                label: info.text,
                data: filtered.map(r=>r.value),
                borderColor: '#667eea',
                backgroundColor: 'rgba(102,126,234,0.2)',
                tension: 0.1,
                fill: true
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } }
    });
}
function exportToCSV() {
    if (!readings.length) { alert('Нет данных'); return; }
    const headers = ['Тип','Значение','Дата'];
    const rows = readings.map(r => { const info = getTypeInfo(r.type); return [info.text, r.value.toFixed(2), r.date]; });
    let csv = headers.join(',') + '\n' + rows.map(row=>row.join(',')).join('\n');
    const blob = new Blob(['\uFEFF'+csv], {type:'text/csv;charset=utf-8;'});
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'meter_readings.csv';
    link.click();
    URL.revokeObjectURL(link.href);
}
function exportToExcel() {
    if (!readings.length) { alert('Нет данных'); return; }
    const headers = ['Тип','Значение','Дата'];
    const rows = readings.map(r => { const info = getTypeInfo(r.type); return [info.text, r.value.toFixed(2), r.date]; });
    let csv = headers.join(',') + '\n' + rows.map(row=>row.join(',')).join('\n');
    const blob = new Blob(['\uFEFF'+csv], {type:'application/vnd.ms-excel;charset=utf-8'});
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'meter_readings.xls';
    link.click();
    URL.revokeObjectURL(link.href);
}
function setDefaultDate() {
    document.getElementById('meterDate').value = new Date().toISOString().split('T')[0];
}
document.addEventListener('DOMContentLoaded', () => {
    loadReadings();
    setDefaultDate();
    loadTheme();
    document.getElementById('addForm').addEventListener('submit', e => {
        e.preventDefault();
        const type = document.getElementById('meterType').value;
        const value = document.getElementById('meterValue').value;
        const date = document.getElementById('meterDate').value;
        if (!value || value<=0) { alert('Введите корректное значение'); return; }
        addReading(type, value, date);
        e.target.reset();
        setDefaultDate();
        document.getElementById('meterValue').focus();
    });
    document.getElementById('clearAllBtn').addEventListener('click', clearAllReadings);
    document.getElementById('themeToggle').addEventListener('click', toggleTheme);
    document.getElementById('updateChartBtn').addEventListener('click', updateChart);
    document.getElementById('exportCsvBtn').addEventListener('click', exportToCSV);
    document.getElementById('exportExcelBtn').addEventListener('click', exportToExcel);
});
