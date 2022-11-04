package calculator;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Calculator extends JFrame {
    JLabel resultLabel;
    JLabel equationLabel;

    String currentNumber = "";
    StringBuilder chain = new StringBuilder();
    boolean negate = false;
    boolean waitNumber = false;
    boolean waitParentheses = false;
    int  parentheses = 0;

    boolean isAction(char ch) {
        return ch == '\u002B' || ch == '-' || ch == '\u00D7' || ch == '\u00F7' || ch == 'r' || ch == 'p';
    }

    boolean isNotNumber(char ch) {
        return (ch <= 47 || ch >= 58) && ch != '.';
    }

    void tapNumber(String number) {
        if (isNotNumber(getLastChar())) {
            currentNumber = "";
        }
        currentNumber += number;
        chain.append(number);
        addText(number);
    }

    void tapAction(String action) {
        String equation = equationLabel.getText();
        int length = equation.length();
        if (length > 0) {
            if (waitNumber && negate) {
                negate = false;
                chain.append(")");
            }
            if (isAction(getLastChar())) {
                chain = new StringBuilder(removeLastChar(chain.toString()));
                equation = removeLastChar(equationLabel.getText());
            } else if (currentNumber.charAt(0) == '.') {
                equation = equation.substring(0, length - currentNumber.length()) + "0" + currentNumber;
            } else if (getLastChar() == '.') {
                equation += "0";
            }
            currentNumber = "";
            chain.append(action);
            equationLabel.setText(equation + action);
        }
    }

    void tapDot() {
        String dot = ".";
        if (isNotNumber(getLastChar())) {
            dot = "0.";
        }
        currentNumber += ".";
        chain.append(dot);
        addText(".");
    }

    void tapPowerTwo() {
        addText("^(2)");
        chain.append("p2");
    }

    void topPowerY() {
        addText("^(");
        chain.append("p");
        waitNumber = true;
        waitParentheses = true;
    }

    void tapSquareRoot() {
        addText("\u221A(");
        chain.append("0.5r");
        waitNumber = true;
        waitParentheses = true;
    }

    void tapPlusMinus() {
        negate = !negate;
        waitParentheses = negate;
        int i = chain.length() - currentNumber.length();
        String equation = equationLabel.getText();
        if (negate) {
            chain = new StringBuilder(chain.substring(0, i) + "(0-" + currentNumber);
            equation = equation.substring(0, i) + "(-" + currentNumber;
        } else {
            chain = new StringBuilder(chain.substring(0, i - 3) + currentNumber);
            equation = equation.substring(0, i - 3) + currentNumber;
        }
        waitNumber = negate;
        equationLabel.setText(equation);
    }

    void tapParentheses() {
        int length = equationLabel.getText().length();
        if (waitParentheses) {
            waitParentheses = false;
            addText(")");
        } else if (length == 0 || isAction(getLastChar()) || getLastChar() == '(') {
            addText("(");
            chain.append("(");
            parentheses++;
        } else {
            addText(")");
            chain.append(")");
            parentheses--;
        }
    }

    String removeLastChar(String str) {
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    void addText(String text) {
        equationLabel.setText(equationLabel.getText() + text);
    }

    double splitEquation(String equation) throws Exception {
        List<Double> numbers = new ArrayList<>();
        List<Character> actions = new ArrayList<>();

        StringBuilder number = new StringBuilder();
        for (int i = 0; i < equation.length(); i++) {
            if (isAction(equation.charAt(i))) {
                actions.add(equation.charAt(i));
                if (number.length() > 0) {
                    numbers.add(Double.parseDouble(number.toString()));
                }
                number = new StringBuilder();
            } else {
                if (equation.charAt(i) == '(') {
                    int parentheses = 0;
                    i++;
                    StringBuilder subEquation = new StringBuilder();
                    while (equation.charAt(i) != ')' || parentheses != 0) {
                        if (equation.charAt(i) == '(') {
                            parentheses++;
                        } else if (equation.charAt(i) == ')') {
                            parentheses--;
                        }
                        subEquation.append(equation.charAt(i));
                        i++;
                    }
                    numbers.add(splitEquation(subEquation.toString()));
                } else {
                    number.append(equation.charAt(i));
                }
            }
        }
        if (number.length() > 0) {
            numbers.add(Double.parseDouble(number.toString()));
        }
        double result = getResult(numbers, actions);
        if (Double.isNaN(result)) {
            throw new Exception();
        }
        return result;
    }

    double getResult(List<Double> numbers, List<Character> actions) {
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) == 'p' || actions.get(i) == 'r') {
                performAction(numbers, actions, i);
                i--;
            }
        }
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) == '\u00D7' || actions.get(i) == '\u00F7') {
                if (actions.get(i) == '\u00F7' && numbers.get(i + 1) == 0) {
                    equationLabel.setForeground(Color.RED.darker());
                    return 0;
                }
                performAction(numbers, actions, i);
                i--;
            }
        }
        for (int i = 0; i < actions.size();) {
            performAction(numbers, actions, i);
        }
        return numbers.get(0);
    }

    void printResult(double number) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        String result = format.format(number).replaceAll(",", ".");
        resultLabel.setText(result);
    }

    char getLastChar() {
        if (equationLabel.getText().length() > 0) {
            return equationLabel.getText().charAt(equationLabel.getText().length() - 1);
        }
        return '\u0000';
    }

    void performAction(List<Double> numbers, List<Character> actions, int index) {
        double num1 = numbers.get(index);
        double num2 = numbers.get(index + 1);
        double result = 0;
        switch (actions.get(index)) {
            case '\u002B':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '\u00D7':
                result = num1 * num2;
                break;
            case '\u00F7':
                result = num1 / num2;
                break;
            case 'p':
                result = Math.pow(num1, num2);
                break;
            case 'r':
                result = Math.sqrt(num2);
                break;
            default:
                break;
        }
        actions.remove(index);
        numbers.remove(index);
        numbers.remove(index);
        numbers.add(index, result);
    }

    void reset() {
        resultLabel.setText("0");
        equationLabel.setText("");
        currentNumber = "";
        equationLabel.setForeground(new Color(0, 180, 0));
        chain = new StringBuilder();
        waitNumber = false;
        negate = false;
        waitParentheses = false;
        parentheses = 0;
    }

    public Calculator() {
        super("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(345, 596);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(211, 211, 211));


        resultLabel = new JLabel("0", SwingConstants.RIGHT);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 40));
        resultLabel.setName("ResultLabel");
        resultLabel.setBounds(16, 26, 300, 40);
        add(resultLabel);

        equationLabel = new JLabel("", SwingConstants.RIGHT);
        equationLabel.setName("EquationLabel");
        equationLabel.setFont(new Font("", Font.BOLD, 13));
        equationLabel.setBounds(16, 99, 300, 13);
        equationLabel.setForeground(new Color(0, 180, 0));
        add(equationLabel);


        JButton parentheses = new JButton("( )");
        parentheses.setName("Parentheses");
        parentheses.setBounds(4, 171, 77, 47);
        parentheses.setBackground(new Color(220, 220, 220));
        parentheses.setBorder(null);
        parentheses.setFont(new Font("", Font.PLAIN, 18));
        parentheses.addActionListener((e) -> tapParentheses());
        add(parentheses);

        JButton clearEntry = new JButton("CE");
        clearEntry.setName("ClearEntry");
        clearEntry.setBounds(84, 171, 77, 47);
        clearEntry.setBackground(new Color(220, 220, 220));
        clearEntry.setBorder(null);
        clearEntry.setFont(new Font("", Font.PLAIN, 18));
        clearEntry.addActionListener((e) -> reset());
        add(clearEntry);

        JButton clear = new JButton("C");
        clear.setName("Clear");
        clear.setBounds(164, 171, 77, 47);
        clear.setBackground(new Color(220, 220, 220));
        clear.setBorder(null);
        clear.setFont(new Font("", Font.PLAIN, 18));
        clear.addActionListener((e) -> reset());
        add(clear);

        JButton delete = new JButton("Del");
        delete.setName("Delete");
        delete.setBounds(244, 171, 77, 47);
        delete.setBackground(new Color(220, 220, 220));
        delete.setBorder(null);
        delete.setFont(new Font("", Font.PLAIN, 18));
        delete.addActionListener((e) -> {
            equationLabel.setText(removeLastChar(equationLabel.getText()));
            if (currentNumber.length() > 0) {
                currentNumber = currentNumber.substring(0, currentNumber.length() - 1);
            }
        });
        add(delete);

        JButton powerTwo = new JButton("X²");
        powerTwo.setName("PowerTwo");
        powerTwo.setBounds(4, 221, 77, 47);
        powerTwo.setBackground(new Color(220, 220, 220));
        powerTwo.setBorder(null);
        powerTwo.setFont(new Font("", Font.ITALIC, 18));
        powerTwo.addActionListener((e) -> tapPowerTwo());
        add(powerTwo);

        JButton powerY = new JButton("Xʸ");
        powerY.setName("PowerY");
        powerY.setBounds(84, 221, 77, 47);
        powerY.setBackground(new Color(220, 220, 220));
        powerY.setBorder(null);
        powerY.setFont(new Font("", Font.ITALIC, 18));
        powerY.addActionListener((e) -> topPowerY());
        add(powerY);

        JButton squareRoot = new JButton("\u221A");
        squareRoot.setName("SquareRoot");
        squareRoot.setBounds(164, 221, 77, 47);
        squareRoot.setBackground(new Color(220, 220, 220));
        squareRoot.setBorder(null);
        squareRoot.setFont(new Font("", Font.PLAIN, 18));
        squareRoot.addActionListener((e) -> tapSquareRoot());
        add(squareRoot);

        JButton divide = new JButton("\u00F7");
        divide.setName("Divide");
        divide.setBounds(244, 221, 77, 47);
        divide.setBackground(new Color(220, 220, 220));
        divide.setBorder(null);
        divide.setFont(new Font("", Font.PLAIN, 18));
        divide.addActionListener((e) -> tapAction("\u00F7"));
        add(divide);

        JButton seven = new JButton("7");
        seven.setName("Seven");
        seven.setBounds(4, 271, 77, 47);
        seven.setBackground(Color.WHITE);
        seven.setBorder(null);
        seven.setFont(new Font("", Font.PLAIN, 18));
        seven.addActionListener((e) -> tapNumber("7"));
        add(seven);

        JButton eight = new JButton("8");
        eight.setName("Eight");
        eight.setBounds(84, 271, 77, 47);
        eight.setBackground(Color.WHITE);
        eight.setBorder(null);
        eight.setFont(new Font("", Font.PLAIN, 18));
        eight.addActionListener((e) -> tapNumber("8"));
        add(eight);

        JButton nine = new JButton("9");
        nine.setName("Nine");
        nine.setBounds(164, 271, 77, 47);
        nine.setBackground(Color.WHITE);
        nine.setBorder(null);
        nine.setFont(new Font("", Font.PLAIN, 18));
        nine.addActionListener((e) -> tapNumber("9"));
        add(nine);

        JButton multiply = new JButton("\u00D7");
        multiply.setName("Multiply");
        multiply.setBounds(244, 271, 77, 47);
        multiply.setBackground(new Color(220, 220, 220));
        multiply.setBorder(null);
        multiply.setFont(new Font("", Font.PLAIN, 18));
        multiply.addActionListener((e) -> tapAction("\u00D7"));
        add(multiply);

        JButton four = new JButton("4");
        four.setName("Four");
        four.setBounds(4, 321, 77, 47);
        four.setBackground(Color.WHITE);
        four.setBorder(null);
        four.setFont(new Font("", Font.PLAIN, 18));
        four.addActionListener((e) -> tapNumber("4"));
        add(four);

        JButton five = new JButton("5");
        five.setName("Five");
        five.setBounds(84, 321, 77, 47);
        five.setBackground(Color.WHITE);
        five.setBorder(null);
        five.setFont(new Font("", Font.PLAIN, 18));
        five.addActionListener((e) -> tapNumber("5"));
        add(five);

        JButton six = new JButton("6");
        six.setName("Six");
        six.setBounds(164, 321, 77, 47);
        six.setBackground(Color.WHITE);
        six.setBorder(null);
        six.setFont(new Font("", Font.PLAIN, 18));
        six.addActionListener((e) -> tapNumber("6"));
        add(six);

        JButton subtract = new JButton("-");
        subtract.setName("Subtract");
        subtract.setBounds(244, 321, 77, 47);
        subtract.setBackground(new Color(220, 220, 220));
        subtract.setBorder(null);
        subtract.setFont(new Font("", Font.PLAIN, 18));
        subtract.addActionListener((e) -> tapAction("-"));
        add(subtract);

        JButton one = new JButton("1");
        one.setName("One");
        one.setBounds(4, 371, 77, 47);
        one.setBackground(Color.WHITE);
        one.setBorder(null);
        one.setFont(new Font("", Font.PLAIN, 18));
        one.addActionListener((e) -> tapNumber("1"));
        add(one);

        JButton two = new JButton("2");
        two.setName("Two");
        two.setBounds(84, 371, 77, 47);
        two.setBackground(Color.WHITE);
        two.setBorder(null);
        two.setFont(new Font("", Font.PLAIN, 18));
        two.addActionListener((e) -> tapNumber("2"));
        add(two);

        JButton three = new JButton("3");
        three.setName("Three");
        three.setBounds(164, 371, 77, 47);
        three.setBackground(Color.WHITE);
        three.setBorder(null);
        three.setFont(new Font("", Font.PLAIN, 18));
        three.addActionListener((e) -> tapNumber("3"));
        add(three);

        JButton add = new JButton("\u002B");
        add.setName("Add");
        add.setBounds(244, 371, 77, 47);
        add.setBackground(new Color(220, 220, 220));
        add.setBorder(null);
        add.setFont(new Font("", Font.PLAIN, 18));
        add.addActionListener((e) -> tapAction("\u002B"));
        add(add);

        JButton plusMinus = new JButton("\u00B1");
        plusMinus.setName("PlusMinus");
        plusMinus.setBounds(4, 421, 77, 47);
        plusMinus.setBackground(Color.WHITE);
        plusMinus.setBorder(null);
        plusMinus.setFont(new Font("", Font.PLAIN, 18));
        plusMinus.addActionListener((e) -> tapPlusMinus());
        add(plusMinus);

        JButton zero = new JButton("0");
        zero.setName("Zero");
        zero.setBounds(84, 421, 77, 47);
        zero.setBackground(Color.WHITE);
        zero.setBorder(null);
        zero.setFont(new Font("", Font.PLAIN, 18));
        zero.addActionListener((e) -> tapNumber("0"));
        add(zero);

        JButton dot = new JButton(".");
        dot.setName("Dot");
        dot.setBounds(164, 421, 77, 47);
        dot.setBackground(Color.WHITE);
        dot.setBorder(null);
        dot.setFont(new Font("", Font.PLAIN, 18));
        dot.addActionListener((e) -> tapDot());
        add(dot);

        JButton equals = new JButton("=");
        equals.setName("Equals");
        equals.setBounds(244, 421, 77, 47);
        equals.setBackground(new Color(220, 220, 220));
        equals.setBorder(null);
        equals.setFont(new Font("", Font.PLAIN, 18));
        equals.addActionListener((e) -> {
            try {
                printResult(splitEquation(chain.toString()));
            } catch (Exception exception) {
                equationLabel.setForeground(Color.RED.darker());
            }
        });
        add(equals);


        setLayout(null);
        setVisible(true);
    }
}