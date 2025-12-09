/**
 * MOTOR TRANSACCIONAL - FRONTEND
 * 
 * Script para manejar el formulario de transacciones (transactions.jsp)
 * Valida datos del cliente antes de enviar al backend
 * Endpoint: POST /transactions
 * 
 * Parámetros:
 *   - type: "PAYMENT" | "REFUND"
 *   - amount: número > 0
 *   - payload: descripción de la transacción
 * 
 * Respuestas:
 *   - 200: Éxito
 *   - 400: Bad Request (validación fallida)
 *   - 500: Error del servidor
 */

// ============================================
// ESTADO Y VARIABLES
// ============================================

let selectedType = 'PAYMENT';
let isProcessing = false;

// ============================================
// ELEMENTOS DEL DOM
// ============================================

const form = document.getElementById('transactionForm');
const typeButtons = document.querySelectorAll('.type-btn');
const typeInput = document.getElementById('typeInput');
const submitBtn = document.getElementById('submitBtn');
const alertBox = document.getElementById('alert');
const payloadInput = document.getElementById('payload');
const amountInput = document.getElementById('amount');
const charCount = document.getElementById('charCount');
const recentSection = document.getElementById('recentTransactions');
const lastTransactionDiv = document.getElementById('lastTransaction');

// ============================================
// INICIALIZACIÓN
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Motor Transaccional iniciado');
    initializeEventListeners();
    setupValidations();
});

// ============================================
// EVENT LISTENERS
// ============================================

function initializeEventListeners() {
    // Selector de tipo de transacción
    typeButtons.forEach(btn => {
        btn.addEventListener('click', handleTypeSelection);
    });

    // Contador de caracteres
    payloadInput.addEventListener('input', updateCharCount);

    // Validación de monto en tiempo real
    amountInput.addEventListener('input', validateAmount);

    // Envío del formulario (con AJAX para mejor UX)
    form.addEventListener('submit', handleFormSubmit);
}

// ============================================
// MANEJO DEL TIPO DE TRANSACCIÓN
// ============================================

function handleTypeSelection(event) {
    event.preventDefault();
    const btn = event.currentTarget;
    
    // Remover clase active de todos
    typeButtons.forEach(b => b.classList.remove('active'));
    
    // Añadir active al seleccionado
    btn.classList.add('active');
    
    // Actualizar tipo
    selectedType = btn.dataset.type;
    typeInput.value = selectedType;
    
    // Actualizar botón submit
    updateSubmitButton();
    
    console.log('📝 Tipo seleccionado:', selectedType);
}

function updateSubmitButton() {
    submitBtn.className = 'submit-btn ' + (selectedType === 'PAYMENT' ? 'payment' : 'refund');
    submitBtn.textContent = selectedType === 'PAYMENT' ? 'Procesar Pago' : 'Procesar Reembolso';
}

// ============================================
// VALIDACIONES
// ============================================

function setupValidations() {
    // Prevenir números negativos
    amountInput.addEventListener('keydown', (e) => {
        if (e.key === '-' || e.key === 'e') {
            e.preventDefault();
        }
    });
}

function validateAmount(event) {
    const value = parseFloat(event.target.value);
    
    if (value < 0) {
        event.target.value = 0;
    }
    
    // Limitar a 2 decimales
    if (event.target.value.includes('.')) {
        const parts = event.target.value.split('.');
        if (parts[1] && parts[1].length > 2) {
            event.target.value = parseFloat(event.target.value).toFixed(2);
        }
    }
}

function updateCharCount() {
    const count = payloadInput.value.length;
    charCount.textContent = count;
    
    // Cambiar color según límite
    if (count > 450) {
        charCount.style.color = '#e74c3c';
    } else if (count > 400) {
        charCount.style.color = '#f39c12';
    } else {
        charCount.style.color = '#999';
    }
}

function validateFormData(amount, payload) {
    // Validar monto
    if (!amount || parseFloat(amount) <= 0) {
        showAlert('⚠️ El monto debe ser mayor a 0', 'error');
        amountInput.focus();
        return false;
    }
    
    // Validar payload
    if (!payload || payload.trim().length === 0) {
        showAlert('⚠️ La descripción es requerida', 'error');
        payloadInput.focus();
        return false;
    }
    
    if (payload.trim().length < 5) {
        showAlert('⚠️ La descripción debe tener al menos 5 caracteres', 'error');
        payloadInput.focus();
        return false;
    }
    
    // Validar tipo
    if (!selectedType || (selectedType !== 'PAYMENT' && selectedType !== 'REFUND')) {
        showAlert('⚠️ Selecciona un tipo de transacción válido', 'error');
        return false;
    }
    
    return true;
}

// ============================================
// MANEJO DEL FORMULARIO
// ============================================

async function handleFormSubmit(event) {
    event.preventDefault();
    
    // Prevenir múltiples envíos
    if (isProcessing) {
        console.warn('⚠️ Ya hay una transacción en proceso');
        return;
    }
    
    // Obtener valores
    const amount = amountInput.value.trim();
    const payload = payloadInput.value.trim();
    
    // Validar
    if (!validateFormData(amount, payload)) {
        return;
    }
    
    // Procesar con AJAX
    await processTransaction(amount, payload);
}

async function processTransaction(amount, payload) {
    isProcessing = true;
    setLoadingState(true);
    
    try {
        console.log('📤 Enviando transacción:', {
            type: selectedType,
            amount: amount,
            payload: payload.substring(0, 50) + '...'
        });
        
        // Crear FormData
        const formData = new URLSearchParams();
        formData.append('type', selectedType);
        formData.append('amount', amount);
        formData.append('payload', payload);
        
        // Enviar petición
        const response = await fetch(form.action, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData.toString()
        });
        
        // Procesar respuesta
        const result = await response.text();
        
        console.log('📥 Respuesta:', {
            status: response.status,
            message: result
        });
        
        handleResponse(response, result, amount, payload);
        
    } catch (error) {
        console.error('❌ Error:', error);
        showAlert('❌ Error de conexión: ' + error.message, 'error');
    } finally {
        isProcessing = false;
        setLoadingState(false);
    }
}

// ============================================
// MANEJO DE RESPUESTAS
// ============================================

function handleResponse(response, result, amount, payload) {
    if (response.ok) {
        // 200 OK - Éxito
        showAlert('✅ ' + result, 'success');
        
        // Extraer ID
        const transactionId = extractTransactionId(result);
        
        // Mostrar en historial
        displayLastTransaction(transactionId, selectedType, amount, payload);
        
        // Limpiar formulario
        resetForm();
        
    } else if (response.status === 400) {
        // 400 Bad Request
        showAlert('❌ Datos inválidos: ' + result, 'error');
        
    } else if (response.status === 500) {
        // 500 Internal Server Error
        showAlert('❌ Error del servidor: ' + result, 'error');
        
    } else {
        // Otro error
        showAlert('❌ Error inesperado (código ' + response.status + '): ' + result, 'error');
    }
}

// ============================================
// UI - ALERTAS Y FEEDBACK
// ============================================

function showAlert(message, type) {
    alertBox.textContent = message;
    alertBox.className = 'alert ' + type;
    alertBox.style.display = 'block';
    
    // Scroll suave
    alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    
    // Auto-ocultar después de 6 segundos
    setTimeout(() => {
        alertBox.style.display = 'none';
    }, 6000);
}

function setLoadingState(loading) {
    submitBtn.disabled = loading;
    
    if (loading) {
        submitBtn.innerHTML = '<span class="loading"></span>Procesando...';
    } else {
        updateSubmitButton();
    }
}

function resetForm() {
    form.reset();
    charCount.textContent = '0';
    charCount.style.color = '#999';
    typeInput.value = selectedType;
    amountInput.focus();
}

// ============================================
// HISTORIAL DE TRANSACCIONES
// ============================================

function extractTransactionId(message) {
    // Buscar patrón "id=XXXXX"
    const match = message.match(/id=([a-zA-Z0-9-]+)/);
    return match ? match[1] : 'N/A';
}

function displayLastTransaction(id, type, amount, payload) {
    const now = new Date().toLocaleString('es-CO', {
        dateStyle: 'short',
        timeStyle: 'medium'
    });
    
    lastTransactionDiv.innerHTML = `
        <p><strong>ID:</strong> ${id}</p>
        <p><strong>Tipo:</strong> ${type === 'PAYMENT' ? '💳 Pago' : '↩️ Reembolso'}</p>
        <p><strong>Monto:</strong> $${parseFloat(amount).toFixed(2)}</p>
        <p><strong>Descripción:</strong> ${payload.substring(0, 100)}${payload.length > 100 ? '...' : ''}</p>
        <p><strong>Fecha:</strong> ${now}</p>
    `;
    
    recentSection.style.display = 'block';
}

// ============================================
// DEBUG (consola del navegador)
// ============================================

// Probar conexión desde consola: testConnection()
window.testConnection = async function() {
    console.log('🧪 Probando conexión...');
    try {
        const response = await fetch(form.action, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'type=PAYMENT&amount=1.00&payload=Test de conexión'
        });
        const result = await response.text();
        console.log('✅ Respuesta:', response.status, result);
    } catch (error) {
        console.error('❌ Error:', error);
    }
};