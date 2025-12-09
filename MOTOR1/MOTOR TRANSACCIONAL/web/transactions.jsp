<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Motor Transaccional - Sistema de Pagos</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="container">
        <!-- Encabezado -->
        <div class="header">
            <h1>🔒 Motor Transaccional</h1>
            <p>Sistema seguro de procesamiento de pagos y reembolsos</p>
            <div class="info-badge">
                <span>🌐 HTTP Endpoint: POST /transactions</span>
                <span>🔌 TCP Socket: Puerto 9000</span>
            </div>
        </div>

        <!-- Alerta de respuesta -->
        <div id="alert" class="alert" style="display: none;"></div>

        <!-- Formulario de transacción -->
        <form id="transactionForm" method="POST" action="${pageContext.request.contextPath}/transactions">
            
            <!-- Selector de tipo de transacción -->
            <div class="type-selector">
                <button type="button" class="type-btn active payment" data-type="PAYMENT">
                    💳 Pago
                </button>
                <button type="button" class="type-btn refund" data-type="REFUND">
                    ↩️ Reembolso
                </button>
            </div>

            <!-- Campo oculto para el tipo -->
            <input type="hidden" id="typeInput" name="type" value="PAYMENT">

            <!-- Campo de monto -->
            <div class="form-group">
                <label for="amount">Monto de la transacción *</label>
                <div class="input-icon">
                    <input 
                        type="number" 
                        id="amount" 
                        name="amount" 
                        step="0.01" 
                        min="0.01"
                        placeholder="0.00" 
                        required
                        aria-label="Monto de la transacción"
                    >
                </div>
                <small class="field-help">Debe ser mayor a 0</small>
            </div>

            <!-- Campo de descripción/payload -->
            <div class="form-group">
                <label for="payload">Descripción / Datos adicionales *</label>
                <textarea 
                    id="payload" 
                    name="payload" 
                    placeholder="Ej: Compra en tienda online, número de orden, detalles del cliente, ID de referencia..."
                    maxlength="500"
                    required
                    aria-label="Descripción de la transacción"
                ></textarea>
                <div class="char-counter">
                    <span id="charCount">0</span>/500 caracteres
                </div>
            </div>

            <!-- Botón de envío -->
            <button type="submit" class="submit-btn payment" id="submitBtn">
                Procesar Transacción
            </button>

            <!-- Información adicional -->
            <div class="form-footer">
                <p>🔐 Todas las transacciones son validadas por el servidor</p>
                <p>📊 Los datos se persisten en la base de datos para auditoría</p>
            </div>
        </form>

        <!-- Historial de última transacción -->
        <div id="recentTransactions" class="recent-section" style="display: none;">
            <h3>📋 Última transacción procesada</h3>
            <div id="lastTransaction" class="transaction-card"></div>
        </div>
    </div>

    <!-- Script de la aplicación -->
    <script src="app.js"></script>
</body>
</html>
