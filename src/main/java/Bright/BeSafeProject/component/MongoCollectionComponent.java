package Bright.BeSafeProject.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "collection-string")
@Getter
@Setter
public class MongoCollectionComponent {
    private String activeCollection;
    private String passiveCollection;

    public void swapActiveCollection(){
        String temp = activeCollection;
        this.activeCollection = this.passiveCollection;
        this.passiveCollection = temp;
    }
}
