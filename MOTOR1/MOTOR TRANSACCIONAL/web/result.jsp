<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resultado - Motor Transaccional</title>
    <link rel="stylesheet" href="style.css">
    <style>
        .result-container {
            max-width: 600px;
            margin: 0 auto;
        }
        
        .result-card {
            background: white;
            padding: 30px;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
        
        .result-icon {
            font-size: 64px;
            margin-bottom: 20px;
        }
        
        .result-icon.success {
            color: #38ef7d;
        }
        
        .result-icon.error {
            color: #e74c3c;
        }
        
        .result-title {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 15px;
            color: #333;
        }
        
        .result-message {
            font-size: 16px;
            color: #666;
            margin-bottom: 25px;
            line-height: 1.6;
        }
        
        .result-details {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 25px;
            text-align: left;
        }
        
        .result-details p {
            margin: 10px 0;
            font-size: 14px;
            color: #555;
        }
        
        .result-details strong {
            color: #333;
            font-weight: 600;
        }
        
        .btn-back {
            display: inline-block;
            padding: 14px 30px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 10px;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        
        .btn-back:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
        }
    </style>
</head>
<body>
    <div class="container result-container">
        <%
            // Obtener parámetros de la petición
            String status = request.getParameter("status");
            String message = request.getParameter("message");
            String transactionId = request.getParameter("id");
            String type = request.getParameter("type");
            String amount = request.getParameter("amount");
            String payload = request.getParameter("payload");
            
            // Valores por defecto si no existen
            boolean isSuccess = "success".equals(status);
            if (message == null) message = "No se recibió mensaje del servidor";
            
            // Fecha actual
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String timestamp = now.format(formatter);
        %>
        
        <div class="result-card">
            <div class="result-icon <%= isSuccess ? "success" : "error" %>">
                <%= isSuccess ? "✅" : "❌" %>
            </div>
            
            <h1 class="result-title">
                <%= isSuccess ? "¡Transacción Exitosa!" : "Error en la Transacción" %>
            </h1>
            
            <p class="result-message">
                <%= message %>
            </p>
            
            <% if (isSuccess && transactionId != null) { %>
                <div class="result-details">
                    <p><strong>ID de Transacción:</strong> <%= transactionId %></p>
                    <p><strong>Tipo:</strong> 
                        <%= "PAYMENT".equals(type) ? "💳 Pago" : "↩️ Reembolso" %>
                    </p>
                    <% if (amount != null) { %>
                        <p><strong>Monto:</strong> $<%= amount %></p>
                    <% } %>
                    <% if (payload != null && !payload.isEmpty()) { %>
                        <p><strong>Descripción:</strong> <%= payload %></p>
                    <% } %>
                    <p><strong>Fecha:</strong> <%= timestamp %></p>
                </div>
            <% } %>
            
            <a href="transactions.jsp" class="btn-back">
                <%= isSuccess ? "Realizar otra transacción" : "Intentar nuevamente" %>
            </a>
        </div>
    </div>
</body>
</html>