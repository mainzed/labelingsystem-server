package v1.utils.retcat;

import link.labeling.retcat.exceptions.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import link.labeling.retcat.classes.RetcatItem;
import link.labeling.retcat.items.RetcatItems;

public class LocalRetcatItems {

    public static List<RetcatItem> getAllRetcatItems() throws IOException, ResourceNotAvailableException  {

        List<RetcatItem> retcatList = new ArrayList();
        retcatList.add(RetcatItems.getRetcatItemByName("This Labeling System"));
        //retcatList.add(RetcatItems.getRetcatItemByName("labeling.link"));
        retcatList.add(RetcatItems.getRetcatItemByName("Getty AAT"));
        retcatList.add(RetcatItems.getRetcatItemByName("Getty TGN"));
        retcatList.add(RetcatItems.getRetcatItemByName("Getty ULAN"));
        retcatList.add(RetcatItems.getRetcatItemByName("Heritage Data Historic England"));
        retcatList.add(RetcatItems.getRetcatItemByName("Heritage Data RCAHMS"));
        retcatList.add(RetcatItems.getRetcatItemByName("Heritage Data RCAHMW"));
        retcatList.add(RetcatItems.getRetcatItemByName("ChronOntology"));
        return retcatList;

    }

}
