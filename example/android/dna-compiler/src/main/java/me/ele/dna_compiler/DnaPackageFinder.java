package me.ele.dna_compiler;

import java.util.ArrayList;
import java.util.List;

public class DnaPackageFinder {

    private List<DnaClassFinder> infoList;

    public DnaPackageFinder() {
        infoList = new ArrayList<>();
    }

    public void addMethodInfo(String packgeName, BaseDnaElement methodInfo) {
        DnaClassFinder temProxy;
        if (infoList == null) {
            infoList = new ArrayList<>();
        }
        if (!infoList.isEmpty()) {
            for (DnaClassFinder packageProxy : infoList) {
                if (packageProxy.getPackageName() != null && packageProxy.getPackageName().equals(packgeName)) {
                    packageProxy.addMethodInfo(methodInfo);
                    return;
                }
            }
        }
        temProxy = new DnaClassFinder(packgeName);
        temProxy.addMethodInfo(methodInfo);
        infoList.add(temProxy);
        return;
    }


    public List<DnaClassFinder> getInfoList() {
        return infoList;
    }
}


