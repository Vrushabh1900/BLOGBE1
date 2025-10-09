package newblogproject.example.newproject.Listeners;

import newblogproject.example.newproject.Events.BlogCreatedEvent;
import org.springframework.context.event.EventListener;

public class BlogCreationListener {

    @EventListener
    public void Sendblogcreationnotif(BlogCreatedEvent blogCreatedEvent)
    {}
    @EventListener
    public void UpdateUser(BlogCreatedEvent blogCreatedEvent)
    {

    }

}
