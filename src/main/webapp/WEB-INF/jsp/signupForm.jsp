<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@ page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
<head>
<title>Jsp page sample</title>
<script src="/js/jquery.min.js"></script>
<script language="javascript">

</script>
</head>
<body>

<form action="/member/signup" method="post">
    <input type="text" name="username" value="test@test.com"/>
    <input type="password" name="password" value="testpassword"/>
    <button type="submit">sign up</button>

</form>


</body>
</html>
