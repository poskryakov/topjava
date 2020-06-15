<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="ru">
<head>
    <title>Meals</title>
</head>
<body>
<h3><a href="index.html">Home</a></h3>
<hr>
<h2>Meals</h2>

<jsp:useBean id="meals" scope="request" type="java.util.List"/>
<jsp:useBean id="FORMATTER" scope="request" type="java.time.format.DateTimeFormatter"/>
<table>
    <tr>
        <th>dateTime</th>
        <th>description</th>
        <th style="text-align:right">calories</th>
        <th>excess</th>
    </tr>
    <c:forEach items="${meals}" var="meal">
        <tr style=${meal.excess ? 'background-color:#FF0000' : 'background-color:#00FF00'}>
            <td>${meal.dateTime.format(FORMATTER)}</td>
            <td>${meal.description}</td>
            <td style="text-align:right">${meal.calories}</td>
            <td>${meal.excess}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>