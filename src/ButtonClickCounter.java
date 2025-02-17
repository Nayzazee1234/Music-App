import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonClickCounter {
    // ตัวแปรสำหรับนับจำนวนการกดปุ่ม
    private static int clickCount = 0;
    
    public static void main(String[] args) {
        // สร้างหน้าต่างหลัก
        JFrame frame = new JFrame("Button Click Counter");
        
        // สร้างปุ่ม
        JButton button = new JButton("Click Me!");
        
        // สร้าง label เพื่อแสดงจำนวนการกด
        JLabel label = new JLabel("Button clicked: 0 times");
        
        // กำหนดให้ปุ่มและ label อยู่ในตำแหน่งที่ต้องการ
        button.setBounds(100, 100, 150, 50);
        label.setBounds(100, 50, 200, 30);
        
        // เพิ่ม ActionListener ให้กับปุ่ม
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clickCount++; // เพิ่มจำนวนการกด
                label.setText("Button clicked: " + clickCount + " times"); // แสดงจำนวนการกด
            }
        });
        
        // ตั้งค่า layout
        frame.setLayout(null);
        frame.add(button);
        frame.add(label);
        
        // ตั้งค่าขนาดของหน้าต่าง
        frame.setSize(400, 300);
        
        // ตั้งให้สามารถปิดหน้าต่างได้
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // แสดงหน้าต่าง
        frame.setVisible(true);
    }
}
