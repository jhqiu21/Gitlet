package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author QIU JINHANG
 */


public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        /**
         * If a user does not input any arguments, print
         * error message and exit
         */
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            /* `init` command */
            case "init":
                isVaildCMD(args, 1);
                Repository.init();
                break;
            /* `add [filename]` command */
            case "add":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.add(args[1]);
                break;
            /* `commit [filename]` command */
            case "commit":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.commit(args[1]);
                break;
            /* `rm [filename]` command */
            case "rm":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.rm(args[1]);
                break;
            /* `log` command */
            case "log":
                isVaildCMD(args, 1);
                Repository.checkInit();
                Repository.log();
                break;
            /* `global-log` command */
            case "global-log":
                isVaildCMD(args, 1);
                Repository.checkInit();
                Repository.global_log();
                break;
            /* `find [commit message]` command */
            case "find":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.find(args[1]);
                break;
            /* `status` command */
            case "status":
                isVaildCMD(args, 1);
                Repository.checkInit();
                Repository.status();
                break;
            /* `checkout` command */
            case "checkout":
                Repository.checkInit();
                switch (args.length) {
                    case 2:
                        Repository.checkoutInBranch(args[1]);
                        break;
                    case 3:
                        if (!args[1].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkout(args[2]);
                        break;
                    case 4:
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkout(args[1], args[3]);
                        break;
                    default:
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                }
                break;
            /* `branch [branch name]` command */
            case "branch":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.branch(args[1]);
                break;
            /* `rm-branch [branch name]` command */
            case "rm-branch":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.rm_branch(args[1]);
                break;
            /* `reset [commit id]` command */
            case "reset":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.reset(args[1]);
                break;
            /* `merge [branch name]` command */
            case "merge":
                isVaildCMD(args, 2);
                Repository.checkInit();
                Repository.merge(args[1]);
                break;
            default:
                /**
                 * If a user inputs a command that doesn’t exist,
                 * print the error message and exit.
                 */
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void isVaildCMD(String[] args, int len) {
        /**
         * Determine whether a cmd is valid
         * If a user inputs a command with the wrong number or format of operands,
         * print the message Incorrect operands. and exit.
         * @param args Input Command
         * @param len valid length of this command
         */
        if (args.length != len) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
