public class TestJobList {

    public static void main (String[] args) {

        try {

            int[][] jobs = {{3, 5}, {6, 4}, {6, 7}};
            JobList list = new JobList(jobs, false);

            int i = 0;
            for (int[] j : jobs) {
                System.out.println(String.format("Job %s: p=%s, d=%s", i, j[0], j[1]));
                i++;
            }


            System.out.println(list);

            System.out.println("Extract Max P");
            int k1 = list.extractMaxP();
            System.out.println(k1);
            System.out.println(list);

            System.out.println("Extract Max P");
            int k2 = list.extractMaxP();
            System.out.println(k2);
            System.out.println(list);

            System.out.println("Extract Max P");
            int k3 = list.extractMaxP();
            System.out.println(k3);
            System.out.println(list);

            System.out.println("Insert 0");
            list.insert(2);
            System.out.println(list);


            System.out.println("Insert 2");
            list.insert(0);
            System.out.println(list);

            System.out.println("Insert 1");
            list.insert(1);
            System.out.println(list);

            System.out.println("Split 1");
            JobList right = list.split(1);
            System.out.println("Left: " + list);
            System.out.println("Right: " + right);

            System.out.println("Move left");
            list.push(right.removeFirst());
            System.out.println("Left: " + list);
            System.out.println("Right: " + right);

            System.out.println("Move left");
            list.push(right.removeFirst());
            System.out.println("Left: " + list);
            System.out.println("Right: " + right);

            System.out.println("Join");
            list.concat(right);
            System.out.println(list);

        }catch(Exception e) {
            e.printStackTrace();
        }

    }

}


