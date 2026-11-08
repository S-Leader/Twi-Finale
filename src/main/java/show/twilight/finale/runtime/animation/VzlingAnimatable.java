package show.twilight.finale.runtime.animation;

import java.util.List;

/**
 * Local animation contract; no classes from the old library are loaded.
 */
public interface VzlingAnimatable {
    List<VzlingAnimation> getAnimations();
}
