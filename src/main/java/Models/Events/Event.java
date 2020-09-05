package Models.Events;
import Models.Payload;
import Models.Repo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private String type;
    private Repo repo;
    private Payload payload;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Repo getRepo() {
        return repo;
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Event{" +
                "type='" + type + '\'' +
                ", repo=" + repo +
                ", payload=" + payload +
                '}';
    }
}
