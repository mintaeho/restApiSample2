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

    var HEADER_TOKEN_KEY = "token";

    function signUp() {
        /*
        var param = {
            username: "test@test.com",
            password: "testpassword"
        };

        $.ajax({
            type: 'POST',
            url: '/member/signup',
            contentType:'application/json; charset=utf-8',
            data: JSON.stringify(param)
        }).done(function(data) {
            console.log(data);
            alert('등록 되었습니다.');
            location.href = "/index?authId="+data;

        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
        */
        location.href = "/member/signup";
    }

    function signIn() {
        /*
        var param = {
            authId: "test@test.com",
            password: "testpassword"
        };

        $.ajax({
            type: 'POST',
            url: '/signIn',
            contentType:'application/json; charset=utf-8',
            data: JSON.stringify(param)
        }).done(function(data) {
            console.log(data);
            alert('로그인 되었습니다.');
            location.href = "/index?authId="+param.authId;

        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
        */

        location.href = "/member/login";

    }

    function test(option) {
        var param = {
            authId: "test@test.com",
            password: "testpassword"
        };

        var userId = $("#userId").val();
        var token = $("#accessToken").val();

        console.log('userId', userId, 'token', token);

        var url = '/test';
        if (option == 'contents') url = '/contents';

        $.ajax({
            type: 'GET',
            url: url,
            headers: {
                    'Authorization': 'Bearer ${token}',
                },
            contentType:'application/text; charset=utf-8',
            //data: JSON.stringify(param),
            /*
            beforeSend: function(xhr) {
                xhr.setRequestHeader("userId", userId);
                xhr.setRequestHeader(HEADER_TOKEN_KEY, token);
                xhr.setRequestHeader("Authorization","JWT " + token);
            }
            */

        }).done(function(data) {
            alert('테스트가 성공 되었습니다.');
            console.log(data);
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    }


</script>
</head>
<body>
    <h1>Simple Login</h1>

    <sec:authorize access="isAuthenticated()">인증됨</sec:authorize>
    <sec:authorize access="isAnonymous()">not login</sec:authorize>




	<div>
	    <ul>

	        <li><a href="javascript:signUp()">
	                <span>sign up</span>
	            </a></li>

	        <li><a href="javascript:signIn()">
	                <span>sign in</span>
	            </a></li>

	        <li><a href="javascript:test()">
	                <span>test</span>
	            </a></li>
	        <li><a href="javascript:test('contents')">
	                <span>contents</span>
	            </a></li>

	    </ul>
	</div>


</body>
</html>
