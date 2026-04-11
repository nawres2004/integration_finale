package tn.esprit.suivie_nawres.utils;

import javafx.scene.layout.Pane;

public final class MedecinNavigationContext {
    private static Pane contentContainer;

    private MedecinNavigationContext() {
    }

    public static void setContentContainer(Pane container) {
        contentContainer = container;
    }

    public static Pane getContentContainer() {
        return contentContainer;
    }
}

