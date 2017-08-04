package lkhj;

import java.util.ArrayList;

/**
 * Solve fractional TSP using two level tree structure
 * Created by Xavier on 2017/6/6.
 */
class TwoLevelTree {

    final private double lower;
    final private double upper;
    private Element[] citysElements;
    private int parentsNum = 0;
    private Parent headParent = null;


    TwoLevelTree(int[] tour) {

        //initialize all city segments
        citysElements = new Element[tour.length];
        for (int i=0; i<tour.length; ++i){
            citysElements[i] = new Element(i);
        }

        lower = Math.sqrt(citysElements.length) / 2;
        upper = lower * 4;

        //build the two level tree
        int stdParentSize = (int)lower*2;
        Parent parent = new Parent();
        for (int i : tour) {
            Element element = citysElements[i];
            parent.addElementToLast(element);
            if (parent.size >= stdParentSize) {
                addParent(parent);
                parent = new Parent();
            }
        }
        if (parent.size > 0 && parent.size < stdParentSize) {
            addParent(parent);
        }
        if (headParent.previousParent.size < lower){
            mergeParBToParA(headParent.previousParent, headParent);
            headParent = headParent.previousParent;
            reSortAllParentID();
        }
    }

//    private void printTour() {
//        int[] tour = getCurrentTour();
//        for (int city : tour) {
//            System.out.print(city + " ");
//        }
//        System.out.println();
//    }

    private void addParent(Parent parent) {
        if (headParent != null) {
            Parent previousLastParent = headParent.previousParent;
            previousLastParent.nextParent = parent;
            parent.nextParent = headParent;
            parent.previousParent = previousLastParent;
            headParent.previousParent = parent;

            parent.endElement.nextElement = headParent.beginElement;
            headParent.beginElement.previousElement = parent.endElement;

            previousLastParent.endElement.nextElement = parent.beginElement;
            parent.beginElement.previousElement = previousLastParent.endElement;
        } else {
            parent.previousParent = parent.nextParent = headParent = parent;
            parent.endElement.nextElement = parent.beginElement;
            parent.beginElement.previousElement = parent.endElement;

            parent.beginElement.previousElement = parent.endElement;
            parent.endElement.nextElement = parent.beginElement;
        }
        parent.ID = parentsNum++;
    }

//    public void test() {
//        //printTour();
//        System.out.println(checkTree());
//
//        Random rand = new Random(0);
//
//        for (long iter = 0; iter < 1000000 ; ++iter){
//            int a = rand.nextInt(citysElements.length);
//            int b = prev(a);
//            int c = rand.nextInt(citysElements.length);
//            int d = next(c);
//
//            if (between(b, a, c) && between(c, d, b)){
//                if(iter %1000 == 0)System.out.println("\n iter " + iter + " move " + b + "-" + a +" "+ c +"-" + d);
//                flip(a, b, c, d);
//
//                //printTour();
//                if (!checkTree()){
//                    throw new Error(String.valueOf(iter));
//                }
//            }
//
//        }
//
//    }

    private boolean checkParentsConnection() {
        if (headParent == null) return false;

        Parent currParent;
        int count;

        //forward direction
        for (count = 1, currParent = headParent.nextParent; currParent != headParent; ++count) {
            if (currParent.nextParent != headParent
                    && currParent.ID != currParent.nextParent.ID - 1) {
                System.err.println("Parent ID error");
                return false;
            }
            currParent = currParent.nextParent;
            if (count > parentsNum){
                System.err.println("parent number error 1");
                return false;
            }
        }
        if (count != parentsNum){
            System.err.println("parent number error 2");
            return false;
        }


        //backward direction
        for (count = 1, currParent = headParent.previousParent; currParent != headParent; ++count) {
            currParent = currParent.previousParent;
            if (count > parentsNum){
                System.err.println("parent number error 3");
                return false;
            }
        }

        return count == parentsNum;
    }

    private boolean checkElementsConnection() {
        Element currElement = headParent.isReverse ? headParent.endElement : headParent.beginElement;
        int count = 0;

        do {
            ++count;
            currElement = currElement.parent.isReverse ? currElement.previousElement : currElement.nextElement;

            if (count > citysElements.length){
                System.err.println("elements number error 1");
                return false;
            }
        } while (currElement != (headParent.isReverse ? headParent.endElement : headParent.beginElement));


        if ( count != citysElements.length){
            System.err.println("elements number error ");
            return false;
        }

        //check reverse
        currElement = headParent.isReverse ? headParent.beginElement : headParent.endElement;
        count = 0;
        do{
            ++count;
            currElement = currElement.parent.isReverse ? currElement.nextElement : currElement.previousElement;
            if (count > citysElements.length){
                System.err.println("elements number error ");
                return false;
            }
        } while (currElement != (headParent.isReverse ? headParent.beginElement : headParent.endElement));

        return count == citysElements.length;
    }

    private boolean checkParentElementsRelation() {
        Parent parent;
        for (parent = headParent; parent != headParent.previousParent; parent = parent.nextParent) {

            if (!parent.checkElementRelation()) return false;
        }
        return parent.checkElementRelation();
    }

    private boolean checkBalance(){
        Parent parent;
        for (parent = headParent; parent != headParent.previousParent; parent = parent.nextParent) {
            if (parent.size < lower || parent.size > upper){
                System.err.println("checkBalance error 1");
                return false;
            }
        }

        return parent.size >= (int) lower && parent.size <= (int) upper;
    }

    boolean checkTree() {
        return checkParentsConnection()
                && checkElementsConnection()
                && checkParentElementsRelation()
                && checkBalance();
    }

    /**
     * find the city next to a
     *
     * @param a index of a
     * @return index of the successor of a
     */
    int next(int a) {
        return citysElements[a].parent.isReverse ? citysElements[a].previousElement.cityID
                : citysElements[a].nextElement.cityID;
    }

    /**
     * find the city in front of a
     *
     * @param a index of a
     * @return index of the city in front of a
     */
    int prev(int a) {
        return citysElements[a].parent.isReverse ? citysElements[a].nextElement.cityID
                : citysElements[a].previousElement.cityID;
    }

    int getHeadCityID(){
        return headParent.isReverse ? headParent.endElement.cityID : headParent.beginElement.cityID;
    }

    boolean between(int a, int b, int c) {
        Element elmA = citysElements[a];
        Element elmB = citysElements[b];
        Element elmC = citysElements[c];

        if (elmA.parent == elmB.parent && elmA.parent == elmC.parent) {
            return elmA.parent.isReverse ?
                    elmA.ID > elmB.ID && elmB.ID > elmC.ID
                            || elmC.ID > elmA.ID && elmA.ID > elmB.ID
                            || elmB.ID > elmC.ID && elmC.ID > elmA.ID
                    :
                    elmA.ID < elmB.ID && elmB.ID < elmC.ID
                            || elmC.ID < elmA.ID && elmA.ID < elmB.ID
                            || elmB.ID < elmC.ID && elmC.ID < elmA.ID;
        } else if (elmA.parent == elmB.parent) {
            return elmA.parent.isReverse ?
                    elmA.ID > elmB.ID
                    :
                    elmA.ID < elmB.ID;
        } else if (elmB.parent == elmC.parent) {
            return elmB.parent.isReverse ?
                    elmB.ID > elmC.ID
                    :
                    elmB.ID < elmC.ID;
        } else if (elmA.parent == elmC.parent) {
            return elmA.parent.isReverse ?
                    elmA.ID < elmC.ID
                    :
                    elmA.ID > elmC.ID;
        } else {
            return elmA.parent.ID < elmB.parent.ID && elmB.parent.ID < elmC.parent.ID
                    || elmC.parent.ID < elmA.parent.ID && elmA.parent.ID < elmB.parent.ID
                    || elmB.parent.ID < elmC.parent.ID && elmC.parent.ID < elmA.parent.ID;
        }
    }

    int[] getCurrentTour() {
        int[] tour = new int[citysElements.length];
        int count = 0;
        Element elm = headParent.isReverse ? headParent.endElement : headParent.beginElement;

        do {
            tour[count++] = elm.cityID;
            elm = elm.parent.isReverse ? elm.previousElement : elm.nextElement;
        } while (elm != (headParent.isReverse ? headParent.endElement : headParent.beginElement));

        return tour;
    }

    /**
     * replace (a,b) and (c,d) by (b,c) and (a,d)
     * a = next(b) d = next(c)
     *
     * @param a index
     * @param b index
     * @param c index
     * @param d index
     */
    void flip(int a, int b, int c, int d) {
        if (a != next(b) || d != next(c)){
            throw new Error("Infeasible flip!");
        }

        //check if d-b and a-c consist of a sequence of consecutive segments
        if (citysElements[a].parent != citysElements[b].parent
                && citysElements[c].parent != citysElements[d].parent) {
            if (ac_IsShorterThan_bd(a, b, c, d)) {
                flipSegments(citysElements[a].parent, citysElements[c].parent);
            } else {
                flipSegments(citysElements[d].parent, citysElements[b].parent);
            }

            //check if a-c lies in one segment
        } else if (liesInOneSegment(a,c)) {
            flipWithinSegment(a, c);

            //check if a-c lies in one segment
        } else if (liesInOneSegment(d,b)) {
            flipWithinSegment(d, b);

        } else {
            ArrayList<Parent> smallParents = splitSegmentToFitFlip(a, b, c, d);
            flip(a, b, c, d);
            mergeParents(smallParents);
        }
    }

    private boolean liesInOneSegment(int a, int c){
        Element elemA = citysElements[a];
        Element elemC = citysElements[c];
        if (elemA.parent != elemC.parent) return false;

        Element elem = elemA;
        while(elem != elemC){
            if (elem.parent != elemA.parent) return false;
            elem = elem.parent.isReverse ? elem.previousElement : elem.nextElement;
        }
        return true;
    }

    private void mergeParents(ArrayList<Parent> parents){
        while(!parents.isEmpty()){
            Parent parent = parents.get(parents.size()-1);
            parents.remove(parents.size()-1);

            if (parent.size > lower)continue;

            if (parent.previousParent.size < parent.nextParent.size){
                mergeParBToParA(parent.previousParent, parent);
            }else{
                Parent nextPar = parent.nextParent;
                mergeParBToParA(parent, nextPar);
                parents.remove(nextPar);
                if (parent.size < lower){
                    parents.add(parent);
                }
            }
        }
//        if (parents[0] != null && parents[0].nextParent == parents[1]){
//            if (parents[0].size + parents[1].size > lower
//                    && parents[0].size + parents[1].size < upper){
//                mergeParBToParA(parents[0], parents[1]);
//            }else{
//                if (parents[0].size < lower){
//                    mergeParBToParA(parents[0].previousParent, parents[0]);
//                }
//                if (parents[1].size < lower){
//                    mergeParBToParA(parents[1], parents[1].nextParent);
//                }
//            }
//        }else if (parents[1] != null && parents[1].nextParent == parents[0]){
//            if (parents[0].size + parents[1].size > lower
//                    && parents[0].size + parents[1].size < upper){
//                mergeParBToParA(parents[1], parents[0]);
//            }else{
//                if (parents[1].size < lower){
//                    mergeParBToParA(parents[1].previousParent, parents[1]);
//                }
//                if (parents[0].size < lower){
//                    mergeParBToParA(parents[0], parents[0].nextParent);
//                }
//            }
//        }else{
//            for (Parent parent : parents){
//            if(parent == null)continue;
//            if (parent.size > lower)continue;
//            if (parent.previousParent.size < parent.nextParent.size){
//                mergeParBToParA(parent.previousParent, parent);
//            }else{
//                mergeParBToParA(parent, parent.nextParent);
//            }
//        }
//        }

        reSortAllParentID();
    }

    private void mergeParBToParA(Parent A, Parent B){
        Element currElem = B.beginElement;
        for(;;){
            currElem.parent = A;
            if (currElem == B.endElement)break;
            currElem = currElem.nextElement;
        }

        A.size += B.size;
        if (!A.isReverse && !B.isReverse){
            A.endElement = B.endElement;
        } else if (A.isReverse && B.isReverse){
            A.beginElement = B.beginElement;
        } else if (!A.isReverse){
            reverseElements(B.beginElement, B.endElement);
            A.endElement.nextElement = B.endElement;
            B.endElement.previousElement = A.endElement;
            A.endElement = B.beginElement;
            A.endElement.nextElement = B.nextParent.isReverse ?
                    B.nextParent.endElement : B.nextParent.beginElement;
            if (B.nextParent.isReverse){
                B.nextParent.endElement.nextElement = A.endElement;
            }else{
                B.nextParent.beginElement.previousElement = A.endElement;
            }
        } else {
            reverseElements(A.beginElement, A.endElement);
            A.beginElement.nextElement = B.beginElement;
            B.beginElement.previousElement = A.beginElement;
            A.beginElement = A.endElement;
            A.endElement = B.endElement;
            A.beginElement.previousElement = A.previousParent.isReverse?
                    A.previousParent.beginElement : A.previousParent.endElement;
            if(A.previousParent.isReverse){
                A.previousParent.beginElement.previousElement = A.beginElement;
            }else {
                A.previousParent.endElement.nextElement = A.beginElement;
            }
            A.isReverse = false;
        }
        A.nextParent = B.nextParent;
        B.nextParent.previousParent = A;
        //B.isAbandoned = true;
        if(headParent == B){
            headParent = A;
        }
        resortElemID(A);
        if (A.size > upper){
            splitByHalf(A);
        }
    }

    private void splitByHalf(Parent parent){
        Parent newPar = new Parent();
        Element cutElem = parent.isReverse ? parent.endElement : parent.beginElement;
        int count = 1;

        while(count < lower * 2){
            cutElem = parent.isReverse ? cutElem.previousElement : cutElem.nextElement;
            ++count;
        }

        newPar.isReverse = parent.isReverse;
        newPar.size = parent.size - count;
        parent.size = count;

        if (newPar.isReverse){
            newPar.beginElement = parent.beginElement;
            parent.beginElement = cutElem;
            newPar.endElement = cutElem.previousElement;
        }else{
            newPar.endElement = parent.endElement;
            parent.endElement = cutElem;
            newPar.beginElement = cutElem.nextElement;
        }

        newPar.nextParent = parent.nextParent;
        newPar.previousParent = parent;
        parent.nextParent.previousParent = newPar;
        parent.nextParent = newPar;

        Element currElem = newPar.isReverse ? newPar.endElement : newPar.beginElement;
        while(currElem != (newPar.isReverse ?
        newPar.beginElement : newPar.endElement)){
            currElem.parent = newPar;
            currElem = newPar.isReverse ? currElem.previousElement : currElem.nextElement;
        }
        currElem.parent = newPar;
        ++parentsNum;
    }

    private void resortElemID(Parent parent){
        int count = 0;
        Element currElem = parent.beginElement;
        do{
            currElem.ID = count++;
            currElem = currElem.nextElement;
        }while(currElem != parent.endElement);
        currElem.ID = count;
    }

    private void reverseElements(Element ElemA, Element ElemB){
        if (ElemA == ElemB)return;
        Element currElem = ElemA;
        Element nextNextElem = ElemA.nextElement;

        while(currElem != ElemB.previousElement){
            Element nextElem = nextNextElem;
            nextNextElem = nextElem.nextElement;
            currElem.previousElement = nextElem;
            nextElem.nextElement = currElem;
            currElem = nextNextElem.previousElement;
        }
        currElem.previousElement = ElemB;
        ElemB.nextElement = currElem;
    }

    private ArrayList<Parent> splitSegmentToFitFlip(int a, int b, int c, int d){
        Parent aPar = citysElements[a].parent;
        Parent bPar = citysElements[b].parent;
        Parent cPar = citysElements[c].parent;
        Parent dPar = citysElements[d].parent;

        ArrayList<Parent> smallParents = new ArrayList<>();

        //split b-a
        if (aPar == bPar){
            splitSegment(b, a, smallParents);
        }

        //split c-d
        if (cPar == dPar){
            splitSegment(c, d, smallParents);
        }

        reSortAllParentID();
        return smallParents;
    }

    private void reSortAllParentID(){
        Parent currPar = headParent;
        int count;
        for (count = 0;
             currPar.nextParent  != headParent;
             ++count, currPar = currPar.nextParent){
            currPar.ID = count;
        }

        currPar.ID = count;
        parentsNum = count + 1;
    }

    private void splitSegment(int b, int a, ArrayList<Parent> smallParents){

        Parent oriPar = citysElements[b].parent;
        Parent newPar = new Parent();

        newPar.size = oriPar.isReverse ?
                citysElements[a].ID - oriPar.beginElement.ID + 1 :
                oriPar.endElement.ID - citysElements[a].ID + 1;
        newPar.isReverse = oriPar.isReverse;
        if (newPar.isReverse){
            newPar.endElement = citysElements[a];
            newPar.beginElement = oriPar.beginElement;
        }else{
            newPar.beginElement = citysElements[a];
            newPar.endElement = oriPar.endElement;
        }

        oriPar.size = oriPar.isReverse ?
                oriPar.endElement.ID - citysElements[b].ID + 1 :
                citysElements[b].ID - oriPar.beginElement.ID + 1;
        if (oriPar.isReverse){
            oriPar.beginElement = citysElements[b];
        }else{
            oriPar.endElement = citysElements[b];
        }

        newPar.nextParent = oriPar.nextParent;
        oriPar.nextParent = newPar;
        newPar.previousParent = oriPar;
        newPar.nextParent.previousParent = newPar;

        Element currElem = newPar.beginElement;
        while(currElem != newPar.endElement){
            currElem.parent = newPar;
            currElem = currElem.nextElement;
        }
        newPar.endElement.parent = newPar;

        if (oriPar.size < lower) smallParents.add(oriPar);
        if (newPar.size < lower) smallParents.add(newPar);
    }

    private boolean ac_IsShorterThan_bd(int a, int b, int c, int d) {
        int aPID = citysElements[a].parent.ID;
        int bPID = citysElements[b].parent.ID;
        int cPID = citysElements[c].parent.ID;
        int dPID = citysElements[d].parent.ID;

        return aPID <= cPID ? cPID - aPID + 1 <= parentsNum / 2
                : bPID - dPID + 1 > parentsNum / 2;
    }

    private void flipSegments(Parent aParent, Parent cParent) {
        Parent currPar = aParent;
        Parent nextNextPar = aParent.nextParent;

        Parent cNextPar = cParent.nextParent;
        Parent aPrevPar = aParent.previousParent;


        while (currPar != cParent) {
            Parent nextPar = nextNextPar;
            nextNextPar = nextPar.nextParent;
            currPar.previousParent = nextPar;
            nextPar.nextParent = currPar;
            currPar.isReverse = !currPar.isReverse;
            currPar = nextNextPar.previousParent;
        }

        currPar.isReverse = !currPar.isReverse;

        aParent.nextParent = cNextPar;
        cNextPar.previousParent = aParent;
        if (aParent.isReverse) {
            if (cNextPar.isReverse) {
                aParent.beginElement.previousElement = cNextPar.endElement;
                cNextPar.endElement.nextElement = aParent.beginElement;
            } else {
                aParent.beginElement.previousElement = cNextPar.beginElement;
                cNextPar.beginElement.previousElement = aParent.beginElement;
            }
        } else {
            if (cNextPar.isReverse) {
                aParent.endElement.nextElement = cNextPar.endElement;
                cNextPar.endElement.nextElement = aParent.endElement;
            } else {
                aParent.endElement.nextElement = cNextPar.beginElement;
                cNextPar.beginElement.previousElement = aParent.endElement;
            }
        }

        cParent.previousParent = aPrevPar;
        aPrevPar.nextParent = cParent;
        if (cParent.isReverse) {
            if (aPrevPar.isReverse) {
                cParent.endElement.nextElement = aPrevPar.beginElement;
                aPrevPar.beginElement.previousElement = cParent.endElement;
            } else {
                cParent.endElement.nextElement = aPrevPar.endElement;
                aPrevPar.endElement.nextElement = cParent.endElement;
            }
        } else {
            if (aPrevPar.isReverse) {
                cParent.beginElement.previousElement = aPrevPar.beginElement;
                aPrevPar.beginElement.previousElement = cParent.beginElement;
            } else {
                cParent.beginElement.previousElement = aPrevPar.endElement;
                aPrevPar.endElement.nextElement = cParent.beginElement;
            }
        }

        reSortParentID(aParent, cParent);
    }

    private void reSortParentID(Parent aParent, Parent cParent) {

        if (aParent == cParent) return;
        Parent dParent = aParent.nextParent;
        Parent bParent = cParent.previousParent;

        if (dParent.ID < bParent.ID && dParent.ID > headParent.ID) {
            Parent currPar = dParent;
            do {
                currPar = currPar.previousParent;
                currPar.ID = currPar.nextParent.ID - 1;
            } while (currPar != cParent);
            currPar.ID = currPar.nextParent.ID - 1;
            headParent = currPar;
        } else {
            Parent currPar = cParent;
            currPar.ID = aParent.ID;
            do {
                currPar = currPar.nextParent;
                currPar.ID = currPar.previousParent.ID + 1;
            } while (currPar != aParent);
        }
    }

    private void flipWithinSegment(int a, int c) {
        Parent currParent = citysElements[a].parent;
        Element currElem = citysElements[a];
        Element preAElem = currParent.isReverse ?
                citysElements[a].nextElement : citysElements[a].previousElement;
        Element nextCElem = currParent.isReverse ?
                citysElements[c].previousElement : citysElements[c].nextElement;
        Element nextNextElem = currElem.parent.isReverse ?
                currElem.previousElement
                :
                currElem.nextElement;

        do {
            if (currElem.parent.isReverse) {
                Element nextElem = nextNextElem;
                nextNextElem = nextElem.parent.isReverse ? nextElem.previousElement : nextElem.nextElement;
                currElem.nextElement = nextElem;
                nextElem.previousElement = currElem;
            } else {
                Element nextElem = nextNextElem;
                nextNextElem = nextElem.parent.isReverse ? nextElem.previousElement : nextElem.nextElement;
                currElem.previousElement = nextElem;
                nextElem.nextElement = currElem;
            }
            currElem = nextNextElem.parent.isReverse ? nextNextElem.nextElement : nextNextElem.previousElement;
        } while (currElem.cityID != c);

        if (preAElem.parent.isReverse) {
            preAElem.previousElement = citysElements[c];
        } else {
            preAElem.nextElement = citysElements[c];
        }
        if (citysElements[c].parent.isReverse) {
            citysElements[c].nextElement = preAElem;
        } else {
            citysElements[c].previousElement = preAElem;
        }

        if (nextCElem.parent.isReverse) {
            nextCElem.nextElement = citysElements[a];
        } else {
            nextCElem.previousElement = citysElements[a];
        }
        if (citysElements[a].parent.isReverse) {
            citysElements[a].previousElement = nextCElem;
        } else {
            citysElements[a].nextElement = nextCElem;
        }

        if (currParent.isReverse){
            if (a== currParent.endElement.cityID){
                currParent.endElement = citysElements[c];
            }
            if (c == currParent.beginElement.cityID){
                currParent.beginElement = citysElements[a];
            }
        }else {
            if (a == currParent.beginElement.cityID) {
                currParent.beginElement = citysElements[c];
            }
            if (c == currParent.endElement.cityID) {
                currParent.endElement = citysElements[a];
            }
        }
        //sort the ids
        currElem = citysElements[c];
        currElem.ID = citysElements[a].ID;
        do {
            Element nextElem = (currParent.isReverse ? currElem.previousElement : currElem.nextElement);
            nextElem.ID = currParent.isReverse ? currElem.ID - 1 : currElem.ID + 1;
            currElem = nextElem;
        } while (currElem != citysElements[a]);
    }

    private class Parent {
        int ID;
        int size = 0;
        boolean isReverse = false;
        Parent previousParent;
        Parent nextParent;
        Element beginElement = null;
        Element endElement = null;

        //boolean isAbandoned = false;

        @Override
        public String toString() {
            return isReverse ? endElement.toString() + '-' + beginElement.toString()
                    : beginElement.toString() + '-' + endElement.toString();
        }

        private void addElementToLast(Element element) {
            if (size == 0) {
                beginElement = element;
                endElement = element;
            } else {
                endElement.nextElement = element;
                element.previousElement = endElement;
                endElement = element;
            }
            element.ID = size++;
            element.parent = this;
        }

        private boolean checkElementRelation() {
            Element element = isReverse ? endElement : beginElement;
            int count = 1;

            while(element != (isReverse ? beginElement : endElement)){
                if (count > size){
                    System.err.println("tour element number error 1");
                    return false;
                }
                if (element.parent != this){
                    System.err.println("tour element number error 2");
                    return false;
                }
                Element followElem = isReverse ? element.previousElement : element.nextElement;

                if (isReverse ?
                        element.ID <= followElem.ID : element.ID >= followElem.ID) {
                    System.err.println("tour element number error 3");
                    return false;
                }

                element = followElem;
                ++count;
            }

            return element.parent == this && count == size;
        }

//        private boolean pathLiesIn(int a, int c) {
//            if (isReverse) {
//                return between(endElement.previousElement.cityID, a, c)
//                        &&
//                        between(a, c, beginElement.nextElement.cityID);
//            } else {
//                return between(beginElement.previousElement.cityID, a, c)
//                        &&
//                        between(a, c, endElement.nextElement.cityID);
//            }
//        }
    }

    private class Element {
        final int cityID;
        int ID;
        Parent parent;
        Element previousElement;
        Element nextElement;

        private Element(int cityID) {
            this.cityID = cityID;
        }

        @Override
        public String toString() {
            return Integer.toString(cityID);
        }
    }
}
