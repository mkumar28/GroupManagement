package com.jira.ultimate.grouplist.GroupMembership.plugin;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.auditing.AuditingManager;
import com.atlassian.jira.auditing.AuditingService;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserRole;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;



@Scanned
public class GroupMembership extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(GroupMembership.class);
	@ComponentImport
	private final TemplateRenderer templateRenderer;
	
	@ComponentImport
    private GlobalPermissionManager globalPermissionManager;

	@ComponentImport
	private final UserManager userManager;
	
	@ComponentImport
	private final GroupManager groupManager;
	@ComponentImport
	private final AuditingService auditingService;

	@ComponentImport
	private final LoginUriProvider loginUriProvider;

	@ComponentImport
	private final PluginSettingsFactory pluginSettingsFactory;
	@ComponentImport
	private final CustomFieldManager CustomFieldManager;
	@ComponentImport
	private final ProjectManager ProjectManager;
	@ComponentImport
	private final AuditingManager auditingManager;

	@ComponentImport
	private final WebSudoManager webSudoManager;
	
	@ComponentImport
	private final SearchRequestManager searchRequestManager;

	@ComponentImport
	private final ApplicationProperties applicationProperties;

	@ComponentImport
	private final GroupPickerSearchService groupPickerSearchService;

	private static final String PLUGIN_STORAGE_KEY = "com.atlassian.plugins.tutorial.refapp.adminui";

	// JiraUserManager userRole = new JiraUserManager();
	@Inject
	public GroupMembership(TemplateRenderer templateRenderer, UserManager userManager,
			LoginUriProvider loginUriProvider, PluginSettingsFactory pluginSettingsFactory,
			CustomFieldManager CustomFieldManager, ProjectManager ProjectManager, AuditingService auditingService,
			AuditingManager auditingManager, WebSudoManager webSudoManager, ApplicationProperties applicationProperties,SearchRequestManager searchRequestManager,
			 GroupManager groupManager,GroupPickerSearchService groupPickerSearchService,GlobalPermissionManager globalPermissionManager
			
	) {
		super();
		this.templateRenderer = templateRenderer;
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.CustomFieldManager = CustomFieldManager;
		this.ProjectManager = ProjectManager;
		this.auditingService = auditingService;
		this.auditingManager = auditingManager;
		this.webSudoManager = webSudoManager;
		this.applicationProperties = applicationProperties;
		this.searchRequestManager = searchRequestManager;
		this.groupManager = groupManager;
		this.groupPickerSearchService = groupPickerSearchService;
		this.globalPermissionManager = globalPermissionManager;
	}

	ArrayList<SearchRequest> searchRequest = new ArrayList<SearchRequest>();	
	Collection<String> GroupList = new ArrayList<String>();	
	Collection<String> groupUser = new ArrayList<String>();	
	Collection<Group> groups = new ArrayList<Group>();	
	Collection<String> group = new ArrayList<String>();
	Collection<ApplicationUser> applicationUserActive = new ArrayList<ApplicationUser>();	
	Collection<SearchRequest> searchRequest_history = new ArrayList<SearchRequest>();	
	Map<String, Object> serachfilters = new HashMap<String, Object>();
	Map<String, Object> JiraUsers = new HashMap<String, Object>();

	String valueToBeEntered = null;
	String nameToBeEntered = null;
	String previousUser = "";
	String description ="";
	String groupName = "";
	SearchRequest sr;
    int count = 0;
    int userExistCount = 0;
    int gpExistCount = 0;
    
    Properties p=new Properties();
    InputStream is = getClass().getClassLoader().getResourceAsStream("GroupMembership.properties");
    
   
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		/**Intitalizing properties file**/
		if (is!=null)
		{
		  p.load(is);
		}
    
		/**Reading the count to make sure groupname field is set to null on first run**/
		   if (count == 0) {
			    groupName = "";
		   }

		//Getting  Current User Loigin
			System.out.println("DO GET METHOD CALL");
	
			//webSudoManager.willExecuteWebSudoRequest(req);
			ApplicationUser appuser = ComponentAccessor.getJiraAuthenticationContext().getUser();
			
			/**If current user is not logged in, redirect to the login page**/
			if (appuser == null) 
					{
						System.out.println("usrProfile is null");
						redirectToLogin(req, resp, false);
						return;
					}	
			/**Check if user exist in the jira group **/
			   groupUser = groupManager.getUserNamesInGroup(p.getProperty("jira-group"));
	
			   for(String gp: groupUser) {
				 ApplicationUser gpAppUser =   userManager.getUserByName(gp);
				 if(gpAppUser.getUsername() == appuser.getUsername()) {
					 userExistCount++;
				 }
			   }
			  
			  if(userExistCount== 0) {
				  System.out.println("usrProfile is null");
					redirectToLogin(req, resp, false);
					return;
			  }
			
			/**Check if requested groupName is part of users GroupList**/
			
			/**Clearing the Collection **/
			serachfilters.clear();
			applicationUserActive.clear();
		    
		
			  
			/**Getting the list of user belong to**/
			GroupList = groupManager.getGroupNamesForUser(appuser);
			for(String gp: GroupList) {
				if(gp.equalsIgnoreCase(groupName)) {
					/**Get all the user from the group*/
					applicationUserActive = groupManager.getUsersInGroup(groupName);
					
				}
			}
			 
			/**Adding the Filters and Active users to the Map with the key**/ 
			serachfilters.put("groupList", GroupList);
			serachfilters.put("activeUsers", applicationUserActive);
			
			/**creating user picker field**/
				 
			System.out.println(serachfilters);
				templateRenderer.render("/templates/GroupMembership.vm", serachfilters, resp.getWriter());
			groupName = "";
			count = count + 1;
			userExistCount = 0;
			
	 
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		
		
		try {
		//Getting user login. Allowing only Project Lead and System Admin to access the page.
		System.out.println("DO POST METHOD CALL");
		//webSudoManager.willExecuteWebSudoRequest(req);
		ApplicationUser appuser = ComponentAccessor.getJiraAuthenticationContext().getUser();
		if (appuser == null)
				{
					System.out.println("usrProfile is null");
					redirectToLogin(req, response, false);
					return;
				}
				String usrName = appuser.getUsername();
			
				response.setContentType("text/html");
				
				
				//Reading vlaues from the screen
				// PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
		        // pluginSettings.put(PLUGIN_STORAGE_KEY + ".name", req.getParameter("name"));
				// pluginSettings.put(PLUGIN_STORAGE_KEY + ".value", req.getParameter("value"));
				//pluginSettings.put(PLUGIN_STORAGE_KEY + ".value", req.getParameter("value"));
				
				
				 groupName = req.getParameter("selectGroup");
		
				 doGet(req, response);
		
				
		} catch (WebSudoSessionException wes) {
			webSudoManager.enforceWebSudoProtection(req, response);
			log.info("Error in Do Post method " + wes.getMessage());
			System.out.println("Error in Do Post method " + wes.getMessage());
		} catch (Exception e) {
			log.info("Error in Do Post method " + e.getMessage());
			System.out.println("Error in Do Post method " + e.getMessage());
		}
	}
	
	

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response, boolean isResponseRedirect)
			throws IOException {
		response.setContentType("text/html;charset=utf-8");
		URI loginURI = loginUriProvider.getLoginUriForRole(getUri(request), UserRole.SYSADMIN);
		if (isResponseRedirect) {
			response.sendRedirect(loginURI.toASCIIString());

		}else{
			response.sendRedirect(applicationProperties.getBaseUrl(UrlMode.ABSOLUTE));
		}
	}

	private URI getUri(HttpServletRequest request) {
		StringBuffer builder = request.getRequestURL();
		log.info("Before URI: " + builder.toString());
		System.out.println("Before URI: " + builder.toString());
		if (request.getQueryString() != null) {
			builder.append("?");
			builder.append(request.getQueryString());
		}
		log.info("After URI: " + builder.toString());
		System.out.println("After URI: " + builder.toString());
		return URI.create(builder.toString());
	}
	
	 

	
}