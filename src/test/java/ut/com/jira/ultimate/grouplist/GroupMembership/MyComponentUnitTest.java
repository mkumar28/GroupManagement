package ut.com.jira.ultimate.grouplist.GroupMembership;

import org.junit.Test;
import com.jira.ultimate.grouplist.GroupMembership.api.MyPluginComponent;
import com.jira.ultimate.grouplist.GroupMembership.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}