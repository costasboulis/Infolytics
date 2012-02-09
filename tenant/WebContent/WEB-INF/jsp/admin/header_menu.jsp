<!--header-->
<!-- hidden id -->
<input type="hidden" id="hiddenTenantId" name="hiddenTenantId" value="${tenant.id}" />
<input type="hidden" id="hiddenTenantToken" name="hiddenTenantToken" value="${tenant.token}" />
<div class="header-wrapper">
	<div class="container">
		<div id="header">
			<div id="divLogo">
				<a href="home.do"><img src="../images/logo_admin.png" border="0" /></a>
			</div>
			<div id="divInfo">
				<div class="content">User logged in: ${tenant.firstName} ${tenant.lastName},
					Company: ${tenant.company}</div>
			</div>
			<div id="divLang">
				<ul class="languages">
					<li><img src="../images/en_flag.png"
							alt="english" border="0" /></li>
					<li style="margin-left: 8px;"><img
							src="../images/el_flag_b.png" alt="greek" border="0" /></li>
				</ul>
				<br clear="right" />
				<div id="divRightMenu">
					<ul class="rightMenu">
						<li><a href="<c:url value="../j_spring_security_logout" />"><span
								class="red">Logout</span></a></li>
						<li><a href="http://www.cleargist.com/contact.php"
							target="_blank">Contact</a></li>
						<li><a href="http://www.cleargist.com/" target="_blank">About</a></li>
					</ul>
				</div>
			</div>

			<div id="divMenu">
				<ul class="menu">
					<% if (selectedMenu == "home") { %><a href="home.do"><li class="selected">Dashboard</li></a><% } else { %><a href="home.do"><li>Dashboard</li></a><% } %>
					<% if (selectedMenu == "catalogue") { %><a href="catalogue.do"><li class="selected">Catalog</li></a><% } else { %><a href="catalogue.do"><li>Catalog</li></a><% } %>
					<% if (selectedMenu == "widgets") { %><a href="widgets.do"><li class="selected">Widgets</a></li><% } else { %><a href="widgets.do"><li>Widgets</li></a><% } %>
					<% if (selectedMenu == "reports") { %><a href="reports.do"><li class="selected">Reports</li></a><% } else { %><a href="reports.do"><li>Reports</li></a><% } %>
					<% if (selectedMenu == "billing") { %><a href="billing.do"><li class="selected">Billing</li></a><% } else { %><a href="billing.do"><li>Billing</li></a><% } %>
					<% if (selectedMenu == "settings") { %><a href="settings.do"><li class="selected">Settings</li></a><% } else { %><a href="settings.do"><li>Settings</li></a><% } %>
					<% if (selectedMenu == "help") { %><a href="help.do"><li class="selected">Help</li></a><% } else { %><a href="help.do"><li>Help</li></a><% } %>
				</ul>
			</div>

		</div>
	</div>
</div>
<!--header ends-->