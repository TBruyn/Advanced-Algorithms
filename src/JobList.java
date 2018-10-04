public class JobList {

    public Node start;
    public Node end;
    public int[][] jobs;

    public int totalP = 0;
    public int length = 0;
    public int maxP = 0;

    /** Runs empty ? O(1) : O(n)  */
    public JobList(int[][] jobs, boolean empty) throws  Exception {
        this.jobs = jobs;
        if(!empty)
            for (int x = 0; x < jobs.length; x++) this.push(x);
        checkLength();
    }

    public JobList(int[][] jobs, Node start, Node end, int length, int totalP) throws  Exception {
        this.jobs = jobs;
        this.start = start;
        this.end = end;
        this.length = length;
        this.totalP = totalP;

        checkLength();
    }

    public void checkLength() throws Exception {
//        int i = 0;
//        int p = 0;
//        int ind = -1;
//        Node curr = start;
//        while(curr != null) {
//            i += 1;
//            p += jobs[curr.index][0];
//
//            if(curr.index <= ind)
//                throw new Exception("Invariant: indices in wrong order");
//            ind = curr.index;
//
//            curr = curr.next;
//        }
//        if(i != length)
//            throw new Exception("Invariant: length is incorrect");
//
//        if(p != totalP)
//            throw new Exception("Invariant: totalP is incorrect");
//
//        if(length != 0 && start == null )
//            throw new Exception("Invarian: start null, length > 0");
//
//        if(length != 0 && end == null )
//            throw new Exception("Invarian: end null, length > 0");
//
//        if(end != null && end.next != null)
//            throw new Exception("Invarian: end.next != null");
//
//        if(length == 1 && start != end)
//            throw new Exception("Invarian: start != end && length == 1");


    }

    /** Runs O(1) */
    public Node push(int x) throws Exception{
        Node n = new Node(x);
        return push(n);
    }

    /** Runs O(1) */
    public Node push(Node n) throws Exception {
        if (length == 0) {
            start = end = n;
        } else {
            end = end.join(n);
        }

        n.join(null); // since our end

        length++;
        totalP += jobs[n.index][0];
        maxP = Math.max(maxP, jobs[n.index][0]); // store the maximum P
        checkLength();
        return n;
    }

    /** Runs O(n) */
    public Node insert(int x) throws Exception {

        if(length == 0)
            return this.push(x);

        Node newNode = new Node(x);
        Node current = start;

        if(current.index > x) {
            newNode.join(start);
            start = newNode;
        } else {
            while (current != null) {
                if (current.next == null) // end
                    return this.push(newNode);
                else if (current.next.index > x)
                    break;

                current = current.next;
            }
            newNode.join(current.next);
            current.join(newNode);
        }

        length++;
        totalP += jobs[x][0];

        checkLength();
        return newNode;
    }

    /** Runs O(n) */
    public int extractMaxP() throws  Exception {

        if(length == 0)
            throw new Exception("No maxP in empty list");

        Node current = start;
        Node prev = null;
        int currP = 0;

        int maxP = 0;
        Node k = null;
        Node beforeK = null;

        // Find k (first node with max processing time)
        while(current != null) {
            currP = jobs[current.index][0];

            if(currP > maxP) {
                k = current;
                beforeK = prev;
                maxP = currP;
            }

            prev = current;
            current = current.next;
        }

        // Remove k
        remove(beforeK, k); // O(1)

        checkLength();
        return k.index;
    }

    /** Runs O(1) */
    private void remove(Node predecessor, Node target) throws Exception { // List must contain these!
        if(predecessor == null) // start
            start = target.next;
        else
            predecessor.join(target.next);

        if(target.next == null) // end
            end = predecessor;

        length--;
        totalP -= jobs[target.index][0];

        checkLength();
    }

    /** Runs O(1) */
    public JobList concat(JobList list) throws  Exception {
        if(list == null)
            throw new Exception("Right list cannot be null");

        if(length == 0) { // |left| is 0, copy right list
            start = list.start;
            end = list.end;
            length = list.length;
            totalP = list.totalP;

        } else if(list.length > 0) { //
            end.join(list.start);
            end = list.end;

            length += list.length;
            totalP += list.totalP;
        }

        checkLength();
        return this;
    }

    /** Runs O(n) */
    public JobList split(int x) throws  Exception{
        int lengthL = 0;
        int totalPL = 0;

        Node current = start;
        Node prev = null;

        while (current != null) {
            if (current.index >= x) {
                // Build the right list
                JobList right = new JobList(
                        jobs,
                        current,
                        end,
                        length - lengthL,
                        totalP - totalPL); // O(1)

                // Correct the left list
                if(current == start) {
                    start = null;
                    end = null;
                } else {
                    this.end = prev;
                    prev.join(null);
                }
                length = lengthL;
                totalP = totalPL;

                return right;
            }

            lengthL += 1;
            totalPL += jobs[current.index][0];
            prev = current;
            current = current.next;
        }
        checkLength();
        return new JobList(jobs, true); // O(1)
    }

    /** Runs O(1) */
    public Node removeFirst()throws  Exception {
        if(this.length == 0)
            throw new Exception("List is empty");

        if(this.length == 1)
            this.end = null;

        Node first = start;
        start = start.next;
        length -= 1;
        totalP -= jobs[first.index][0];

        first.join(null);
        checkLength();
        return first;
    }

    class Node {
        int index;
        Node next;

        public Node(int index) {
            this.index = index;
        }

        /** Runs O(1) */
        public Node join(Node n) {
            next = n;
            return n;
        }
    }

    public String toString() {
        String s = "{";
        Node current = start;
        while(current != null) {
            s += current.index + ",";
            current = current.next;
        }
        return s + "} " + String.format("[ len: %s, totalP: %s ]", length, totalP);
    }

}
