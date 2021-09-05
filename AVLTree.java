/**
 * public class AVLNode
 * <p>
 * This class represents an AVLTree with integer keys and boolean values.

import java.util.ArrayList;
import java.util.List;


public class AVLTree {

    private AVLNode root;
    private AVLNode empty_node = new AVLNode(-1, null);
    private int size;
    private AVLNode min_node = root;
    private AVLNode max_node = root;
    /**
     * This constructor creates an empty AVLTree.
     */
    public AVLTree(){
        this.root = empty_node;
        this.size = 0;
    }

    /**
     * public boolean empty()
     * <p>
     * returns true if and only if the tree is empty
     */
    //Complexity: O(1)
    public boolean empty() {
        return !root.isRealNode(); // to be replaced by student code
    }

    /**
     * public boolean search(int k)
     * <p>
     * returns the info of an item with key k if it exists in the tree
     * otherwise, returns null
     */
    //Complexity: O(log(n))
    public Boolean search(int k) {
        return search_node(k).getValue();
    }

    // Binary search of node with key k returning pointer to that node or pointer to virtual node if does not exist.
    //Complexity: O(log(n))
    public AVLNode search_node(int k){
        AVLNode curr = this.root;
        while(curr.isRealNode()){
            if (curr.getKey()==k)return curr;
            if (curr.getKey()>k)curr = curr.getLeft();
            else curr = curr.getRight();
        }
        return empty_node;
    }

    /**
     * public int insert(int k, boolean i)
     * <p>
     * inserts an item with key k and info i to the AVL tree.
     * the tree must remain valid (keep its invariants).
	 * returns the number of nodes which require rebalancing operations (i.e. promotions or rotations).
	 * This always includes the newly-created node.
     * returns -1 if an item with key k already exists in the tree.
     */

    /*This function inserts a new node to the tree with key k and value i. Complexity: O(log(n))
     * The function operates in 4 steps:
     * 1. Searches and finds the proper place to insert the Node. Complexity: O(log(n))
     * 2. Inserts the Node as a leaf. Complexity: O(1)
     * 3. Ascends from the inserted leaf calculating balance factors,
     *    updating heights and determining if rotation is necessary. Complexity: O(log(n))
     * 4. Performs the proper rotations using leftRotation and rightRotation functions. Complexity: O(1)
     * */
    public int insert(int k, boolean i) {
        int oper_count = 1;
        this.size++;
        AVLNode curr = this.getRoot();
        if (!curr.isRealNode()){ // This is the case if the tree is empty
            this.root = new AVLNode(k, i);
            this.root.setParent(empty_node);
            this.root.setLeft(empty_node);
            this.root.setRight(empty_node);
            this.root.updateTrues_in_sub_tree();
            min_node = this.root; // Maintaining the fields of minimum and maximum node
            max_node = this.root;
            return oper_count;
        }
        AVLNode explorer = curr; // Step 1: Searching for the place to insert the node
        int true_val = (i)? 1: 0; // Maintaining the field of the sum of trues in sub tree
        while (explorer.isRealNode()){
            curr = explorer;
            curr.trues_in_sub_tree+=true_val;
            if (explorer.getKey()==k){ // If we found an existing node with the same key. No insert will take place
                ascendUpdateTrues(explorer); // We will ascend back to root fixing the field trues_in_sub_tree
                this.size--;
                return -1;
            }
            if (explorer.getKey()>k)explorer = explorer.getLeft();
            else explorer = explorer.getRight();
        }
        if(curr.getKey()>k){ // Step 2: The actual insert
            curr.setLeft(new AVLNode(k, i));
            curr.getLeft().setParent(curr);
            curr = curr.getLeft();
        }
        else{
            curr.setRight(new AVLNode(k, i));
            curr.getRight().setParent(curr);
            curr = curr.getRight();
        }
        curr.setRight(empty_node);
        curr.setLeft(empty_node);
        curr.updateHeight();
        if(k> max_node.getKey()) max_node = curr;
        if(k< min_node.getKey()) min_node = curr;
        curr.updateTrues_in_sub_tree();
        AVLNode temp = curr.getParent(); // Step 3
        while (temp!=null&&temp.isRealNode()){
            int bf = temp.getBalanceFactor();
            if(Math.abs(bf)<2){
                int new_height = Math.max(temp.getLeft().getHeight(), temp.getRight().getHeight())+1;
                if(temp.getHeight() == new_height)return oper_count;
                else{
                    temp.updateHeight();
                    temp.updateTrues_in_sub_tree();
                    temp = temp.getParent();
                    oper_count++;
                }
            }
            else if (bf==2){ // left then right rotation
                AVLNode a = temp.getLeft();
                AVLNode parent = temp.getParent();
                if (a.getBalanceFactor()==-1){
                    AVLNode b = a.getRight();
                    int lr = leftRotation(b, a, temp);
                    rightRotation(b, temp, parent);
                    parent.updateHeight();
                    parent.updateTrues_in_sub_tree();
                    return oper_count+lr;
                }
                else // Just right rotation
                    if (a.getBalanceFactor()==1){
                        parent.updateHeight();
                        parent.updateTrues_in_sub_tree();
                        return oper_count+rightRotation(a, temp, parent);
                    }
            }
            else if (bf == -2){ //right then left rotation
                AVLNode a = temp.getRight();
                AVLNode parent = temp.getParent();
                if (a.getBalanceFactor()==1){
                    AVLNode b = a.getLeft();
                    int rr = rightRotation(b, a, temp);
                    int lr = leftRotation(b, temp, parent);
                    parent.updateHeight();
                    parent.updateTrues_in_sub_tree();
                    return oper_count+lr;
                }
                else // Just left rotation
                    if (a.getBalanceFactor()==-1){
                        parent.updateHeight();
                        parent.updateTrues_in_sub_tree();
                        return oper_count+leftRotation(a, temp, parent);
                    }
            }
            else break;
        }
        if(temp==null)System.out.println(k);
        return oper_count;
    }

    /*
     * public int leftRotation(AVLNode pivot, AVLNode right_son, AVLNode parent)
     * Rotates nodes right_son, pivot and grnd_son to the left so the pivot takes the place of right_son.
     * the tree must remain valid (keep its invariants).
     * returns 1 as the number of rebalancing operations.
     * Complexity: O(1)
     */
    public int leftRotation(AVLNode right, AVLNode pivot, AVLNode parent){
        AVLNode grnd_son = right.getRight();
        //Attach temps old parent as pivots new parent
        if(parent.isRealNode() && parent.getKey()<pivot.getKey()) parent.setRight(right);
        else if(parent.isRealNode() && parent.getKey()>pivot.getKey()) parent.setLeft(right);
        else this.root = right;
        right.setParent(parent);
        pivot.setRight(right.getLeft());
        pivot.getRight().setParent(pivot);
        pivot.setParent(right);
        right.setLeft(pivot);
        pivot.updateHeight();
        pivot.updateTrues_in_sub_tree();
        right.updateHeight();
        right.updateTrues_in_sub_tree();
        return 1;
    }
    /*
     * public int rightRotation(AVLNode pivot, AVLNode left_son, AVLNode parent)
     * Rotates nodes left_son, pivot and grnd_son to the left so the pivot takes the place of left_son.
     * the tree must remain valid (keep its invariants).
     * returns 1 as the number of rebalancing operations.
     * Complexity: O(1)
     */
    public int rightRotation(AVLNode left, AVLNode pivot, AVLNode parent){
        AVLNode grnd_son = left.getLeft();
        //Attach temps old parent as pivots new parent
        if(parent.isRealNode() && parent.getKey()<pivot.getKey()) parent.setRight(left);
        else if(parent.isRealNode() && parent.getKey()>pivot.getKey()) parent.setLeft(left);
        else this.root = left;
        left.setParent(parent);
        pivot.setLeft(left.getRight());
        pivot.getLeft().setParent(pivot);
        pivot.setParent(left);
        left.setRight(pivot);
        pivot.updateHeight();
        pivot.updateTrues_in_sub_tree();
        left.updateHeight();
        left.updateTrues_in_sub_tree();
        return 1;
    }

    /**
     * public Boolean HasRightSon(AVLNode node)
     * returns true if node has a right son that is a real node and not virtual
     * Complexity: O(1)
     */
    public Boolean HasRightSon(AVLNode node) {
        if (node.getRight().isRealNode() ) {
            return true;
        }
        return false;
    }
    /**
     * public Boolean HasLeftSon(AVLNode node)
     * returns true if node has a left son that is a real node and not virtual
     * Complexity: O(1)
     */
    public Boolean HasLeftSon(AVLNode node) {
        if (node.getLeft().isRealNode() ) {
            return true;
        }
        return false;
    }

    /**
     * public int delete(int k)
     * <p>
     * deletes an item with key k from the binary tree, if it is there;
     * the tree must remain valid (keep its invariants).
     * returns the number of nodes which required rebalancing operations (i.e. demotions or rotations).
     * returns -1 if an item with key k was not found in the tree.
     */
    /*This function deletes the node with key k if exists in the tree. Complexity: O(log(n))
     * The function operates in 4 steps:
     * 1. Searches and finds the node to delete. Complexity: O(log(n))
     * 2. Deletes the node and replaces it with its single son or its successor if it has two sons,
     *    using successor function. Complexity: O(log(n))
     * 3. Ascends from the deleted node (or the successor node) calculating balance factors,
     *    updating heights and determining if rotation is necessary. Complexity: O(log(n))
     * 4. Performs the proper rotations using leftRotation and rightRotation functions. Complexity: O(1)
     * */
    public int delete(int k) {
        if (size() == 1) { // if delete the root and he is a leaf
            this.root = empty_node;
            min_node = empty_node;
            max_node = empty_node;
            this.size--;
            return 0;
        }
        int oper_count = 0;
        AVLNode to_del = search_node(k);
        if(!to_del.isRealNode()) return -1;
        if(to_del==this.max_node)this.max_node=predecessor(to_del);//Maintaining minimum and maximum node pointers using
        if(to_del==this.min_node)this.min_node=successor(to_del);  //predecessor and successor functions. O(log(n))
        this.size--;
        AVLNode start_of_rotations = null;
        if(!HasLeftSon(to_del)&&!HasRightSon(to_del))changeKid(to_del, empty_node); // to_del is leaf
        else if(HasLeftSon(to_del)&&!HasRightSon(to_del))changeKid(to_del,to_del.getLeft()); //to_del has only left son
        else if(!HasLeftSon(to_del)&&HasRightSon(to_del))changeKid(to_del,to_del.getRight());//to_del has only right son
        else{ // to_del has both sons so we need to look for the successor.
            AVLNode succ = successor(to_del);
            if(succ.getParent()==to_del) start_of_rotations=succ;
            else start_of_rotations=succ.getParent();
            if(HasRightSon(succ))changeKid(succ, succ.getRight());
            else changeKid(succ, empty_node);
            succ.setLeft(to_del.getLeft());
            succ.setRight(to_del.getRight());
            succ.getRight().setParent(succ);
            succ.getLeft().setParent(succ);
            succ.height= to_del.getHeight();
            succ.trues_in_sub_tree=to_del.getTrues_in_sub_tree();
            changeKid(to_del, succ);
        }
        if(start_of_rotations==null)start_of_rotations=to_del.getParent();
        AVLNode current = start_of_rotations; //Beginning the rotation process
        while (current.isRealNode()) {
            int new_height = Math.max(current.getLeft().getHeight(), current.getRight().getHeight()) + 1;
            if (Math.abs(current.getBalanceFactor()) < 2) {
                if (current.getHeight() == new_height){
                    ascendUpdateTrues(current); // Finish by ascending to root updating trues in sub_tree
                    return oper_count;
                    //current = current.getParent();
                }
                else {
                    current.updateTrues_in_sub_tree();
                    current.updateHeight();
                    current = current.getParent();
                    oper_count++;
                }
            }
            else if (current.getBalanceFactor() == 2) {
                if (current.getLeft().getBalanceFactor() != -1) {
                    oper_count += rightRotation(current.getLeft(), current, current.getParent());
                }
                else {
                    leftRotation(current.getLeft().getRight(), current.getLeft(), current);
                    oper_count += rightRotation(current.getLeft(), current, current.getParent());
                }
                current = current.getParent().getParent();
            }
            else if (current.getBalanceFactor() == -2) {
                if (current.getRight().getBalanceFactor() != 1) {
                    oper_count += leftRotation(current.getRight(), current, current.getParent());
                }
                else {
                    rightRotation(current.getRight().getLeft(), current.getRight(), current);
                    oper_count += leftRotation(current.getRight(), current, current.getParent());
                }
                current = current.getParent().getParent();
            }
            else break;
        }
        return oper_count;
    }
    /*
    * Function during delete we may need to ascend to the root updating the field trues_in_sub_tree
    * Complexity: O(log(n))
    * */
    public void ascendUpdateTrues(AVLNode node){
        while (node!=empty_node){
            node.updateTrues_in_sub_tree();
            node=node.getParent();
        }
    }
    // This function sets Node's parents child as new_kid in place of Node
    // Or uses this.root if Node is the root
    // Complexity: O(1)
    public void changeKid(AVLNode Node, AVLNode new_kid){
        if(Node==this.root){
            this.root=new_kid;
            new_kid.setParent(empty_node);
            return;
        }
        AVLNode parent = Node.getParent();
        if(parent.getLeft()==Node)parent.setLeft(new_kid);
        else if(parent.getRight()==Node)parent.setRight(new_kid);
        if(new_kid.isRealNode())new_kid.setParent(parent);
    }

    /**
     * public Boolean min()
     * <p>
     * Returns the info of the item with the smallest key in the tree,
     * or null if the tree is empty
     */
    // Complexity: O(1)
    public Boolean min() {
        return(this.empty())? null : min_node.getValue();
    }

    /**
     * public Boolean max()
     * <p>
     * Returns the info of the item with the largest key in the tree,
     * or null if the tree is empty
     */
    // Complexity: O(1)
    public Boolean max() {
        return(this.empty())? null : max_node.getValue();
    }

    /**
     * public int[] keysToArray()
     * <p>
     * Returns a sorted array which contains all keys in the tree,
     * or an empty array if the tree is empty.
     */
    public int[] keysToArray() {
        int[] arr = new int[size()]; // to be replaced by student code
        if (root.isRealNode()) {
            List<Integer> lst = new ArrayList<Integer>(size());
            keysToArrayRec(lst, root);
            int index = 0;
            for (Integer i : lst) {
                arr[index] = i;
                index++;
            }
        }
        return arr;              // to be replaced by student code
    }
    private void keysToArrayRec(List<Integer> lst, AVLNode root) {
        if (root.getValue() != null) {
            keysToArrayRec(lst, root.getLeft());
            lst.add(root.getKey());
            keysToArrayRec(lst, root.getRight());
        }
    }

    /**
     * public boolean[] infoToArray()
     * <p>
     * Returns an array which contains all info in the tree,
     * sorted by their respective keys,
     * or an empty array if the tree is empty.
     */
    public boolean[] infoToArray() {
        boolean[] arr = new boolean[this.size()];
        if (root.isRealNode()) {
            List<Boolean> lst = new ArrayList<Boolean>();
            infoToArrayRec(lst, root);
            int index = 0;
            for (Boolean i : lst) {
                arr[index] = i;
                index++;
            }
        }
        return arr;
    }

    private void infoToArrayRec(List<Boolean> lst, AVLNode root) {
        if (root.getValue() != null) {
            infoToArrayRec(lst, root.getLeft());
            lst.add(root.getValue());
            infoToArrayRec(lst, root.getRight());
        }
    }


    /**
     * public int size()
     * <p>
     * Returns the number of nodes in the tree.
     */
    // Complexity: O(1)
    public int size() { return this.size;  }

    /**
     * public int getRoot()
     * <p>
     * Returns the root AVL node, or null if the tree is empty
     */
    // Complexity: O(1)
    public AVLNode getRoot() { return this.root; }

    /**
     * public boolean prefixXor(int k)
     *
     * Given an argument k which is a key in the tree, calculate the xor of the values of nodes whose keys are
     * smaller or equal to k.
     *
     * precondition: this.search(k) != null
     *
     */
    /*
    * Complexity: O(log(n))
    * */
    public boolean prefixXor(int k){
        int true_count = 0;
        AVLNode curr = this.getRoot();
        while(curr.isRealNode() && curr.getKey()!=k){
            if(curr.getKey()<k){
                true_count+=curr.getTrues_in_sub_tree()-curr.getRight().getTrues_in_sub_tree();
                curr=curr.getRight();
            }
            else curr = curr.getLeft();
        }
        int my_true = (curr.getValue())? 1 : 0;
        true_count+=curr.getLeft().getTrues_in_sub_tree() + my_true;

        return !(true_count%2==0);
    }

  /*
  * Return the predecessor of node (or virtual node if predecessor does not exist)
  * Complexity: O(log(n))
  * */
    public AVLNode predecessor(AVLNode node){
        if (!node.isRealNode()) return empty_node;
        AVLNode parent;
        if(HasLeftSon(node)){
            node = node.getLeft();
            while(node.getRight().isRealNode())node= node.getRight();
            return node;
        }
        else{
            while(node.getParent().isRealNode()){
                parent = node.getParent();
                if(parent.getRight().isRealNode()&&parent.getRight()==node) return parent;
                node = parent;
            }
            return empty_node;
        }
    }


    /**
     * public AVLNode successor
     *
     * given a node 'node' in the tree, return the successor of 'node' in the tree (or null if successor doesn't exist)
     *
     * @param node - the node whose successor should be returned
     * @return the successor of 'node' if exists, null otherwise
     */
    // Complexity: O(log(n))
    public AVLNode successor(AVLNode node) {
        if (node == this.max_node)return null;
        AVLNode suc = null;
        if(HasRightSon(node)){
            suc = node.getRight();
            while (HasLeftSon(suc))suc=suc.getLeft();
            return suc;
        }
        suc=node;
        while (suc.getParent().isRealNode()&&suc.getParent().getRight()==suc)suc=suc.getParent();
        return (suc==null)? suc: suc.getParent();

    }

    /**
     * public boolean succPrefixXor(int k)
     *
     * This function is identical to prefixXor(int k) in terms of input/output. However, the implementation of
     * succPrefixXor should be the following: starting from the minimum-key node, iteratively call successor until
     * you reach the node of key k. Return the xor of all visited nodes.
     *
     * precondition: this.search(k) != null
     */
    // Complexity: O(nlog(n))
    public boolean succPrefixXor(int k){
        if(k==min_node.getKey())return min();
        int xor_count = 0;
        AVLNode curr = min_node;
        if (!curr.isRealNode())return false;
        if (curr.getValue())xor_count++;
        AVLNode succ = successor(curr);
        while (succ!=null&&succ.isRealNode()){
            if(succ.getValue())xor_count++;
            if(succ.getKey()==k)break;
            else succ=successor(succ);
        }

        return (xor_count%2!=0);
    }


    /**
     * public class AVLNode
     * <p>
     * This class represents a node in the AVL tree.
     * <p>
     * IMPORTANT: do not change the signatures of any function (i.e. access modifiers, return type, function name and
     * arguments. Changing these would break the automatic tester, and would result in worse grade.
     * <p>
     * However, you are allowed (and required) to implement the given functions, and can add functions of your own
     * according to your needs.
     */
    public class AVLNode {

        private int key;
        private Boolean value;
        private AVLNode left;
        private AVLNode right;
        private AVLNode parent;
        private int height;
        private int trues_in_sub_tree;


        //Returns balance factor using height of left and right child
        // Complexity: O(1)
        public int getBalanceFactor(){
            if (this.isRealNode()) return this.left.getHeight()-this.right.getHeight();
            return 0;
        }


        // This constructor sets the value, the key, the height to 0
        // if constructing a fake empty node (null node, non-existent node)  height = -1
        public AVLNode(int k, Boolean val){
            this.value = val;
            this.key = k;
            this.height = (k==-1) ? -1 : 0;
            this.trues_in_sub_tree = (val!=null&&val)? 1:0;
        }

        /* Returns the amount of nodes with value true in subtree
         * Complexity: O(1)
         * */
        public int getTrues_in_sub_tree(){return this.trues_in_sub_tree;}

        /* Updates the amount of nodes with value true in subtree by adding trues in subtree of left and right sons
         * and 1 if self value is true
         * Complexity: O(1)
         * */
        public void updateTrues_in_sub_tree(){
            if(!this.isRealNode())return;
            this.trues_in_sub_tree = this.getRight().getTrues_in_sub_tree() + this.getLeft().getTrues_in_sub_tree();
            if(this.getValue())this.trues_in_sub_tree++;
        }

        //returns node's key (for virtual node return -1)
        // Complexity: O(1)
        public int getKey() {
            return this.key;
        }

        //returns node's value [info] (for virtual node return null)
        // Complexity: O(1)
        public Boolean getValue() {
            return this.value;
        }

        //sets left child
        // Complexity: O(1)
        public void setLeft(AVLNode node) {
            this.left = node;
            return;
        }

        //returns left child (if there is no left child return null)
        // Complexity: O(1)
        public AVLNode getLeft() {
            return this.left;
        }

        //sets right child
        // Complexity: O(1)
        public void setRight(AVLNode node) {
            this.right = node;
            return;
        }

        //returns right child (if there is no right child return null)
        // Complexity: O(1)
        public AVLNode getRight() {
            return this.right;
        }

        //sets parent
        // Complexity: O(1)
        public void setParent(AVLNode node) {
            this.parent = node;
            return;
        }

        //returns the parent (if there is no parent return virtual AVL node)
        // Complexity: O(1)
        public AVLNode getParent() {
            return this.parent;
        }

        // Returns True if this is a non-virtual AVL node
        // Complexity: O(1)
        public boolean isRealNode() {
            return (this.value!=null);
        }

        // sets the height of the node
        // Complexity: O(1)
        public void setHeight(int height) {
            this.height = height;
            return;
        }

        /* Updates the height of node by getting max height of left and right sons
         * and adding 1
         * Complexity: O(1)
         * */
        public void updateHeight(){
            //if(!this.getRight().isRealNode() && !this.getLeft().isRealNode()) this.height = 0;
            if (this.isRealNode()) this.height = Math.max(this.getLeft().getHeight(), this.getRight().getHeight())+1;
        }

        // Returns the height of the node
        // Complexity: O(1)
        public int getHeight() {
            return this.height;
        }
    }
}
