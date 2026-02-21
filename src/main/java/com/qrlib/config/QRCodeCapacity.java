package com.qrlib.config;

public class QRCodeCapacity {

    private final int totalDataCodewords;
    private final int ecCodewords;

    private QRCodeCapacity(int totalDataCodewords, int ecCodewords) {
        this.totalDataCodewords = totalDataCodewords;
        this.ecCodewords = ecCodewords;
    }

    public int getTotalDataCodewords() {
        return totalDataCodewords;
    }

    public int getEcCodewords() {
        return ecCodewords;
    }

    public static QRCodeCapacity getCapacity(QRCodeVersion version, ECCLevel eccLevel) {
        int v = version.getValue();
        switch (v) {
            case 1:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(19, 7);
                    case M: return new QRCodeCapacity(16, 10);
                    case Q: return new QRCodeCapacity(13, 13);
                    case H: return new QRCodeCapacity(9, 17);
                }
                break;
            case 2:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(34, 10);
                    case M: return new QRCodeCapacity(28, 16);
                    case Q: return new QRCodeCapacity(22, 22);
                    case H: return new QRCodeCapacity(16, 28);
                }
                break;
            case 3:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(55, 15);
                    case M: return new QRCodeCapacity(44, 26);
                    case Q: return new QRCodeCapacity(34, 36);
                    case H: return new QRCodeCapacity(26, 44);
                }
                break;
            case 4:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(80, 20);
                    case M: return new QRCodeCapacity(64, 36);
                    case Q: return new QRCodeCapacity(48, 52);
                    case H: return new QRCodeCapacity(36, 64);
                }
                break;
            case 5:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(108, 26);
                    case M: return new QRCodeCapacity(86, 48);
                    case Q: return new QRCodeCapacity(30, 36);
                    case H: return new QRCodeCapacity(22, 44);
                }
                break;
            case 6:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(136, 36);
                    case M: return new QRCodeCapacity(108, 64);
                    case Q: return new QRCodeCapacity(76, 96);
                    case H: return new QRCodeCapacity(60, 112);
                }
                break;
            case 7:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(156, 40);
                    case M: return new QRCodeCapacity(124, 72);
                    case Q: return new QRCodeCapacity(28, 36);
                    case H: return new QRCodeCapacity(52, 104);
                }
                break;
            case 8:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(194, 48);
                    case M: return new QRCodeCapacity(76, 44);
                    case Q: return new QRCodeCapacity(72, 88);
                    case H: return new QRCodeCapacity(56, 104);
                }
                break;
            case 9:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(232, 60);
                    case M: return new QRCodeCapacity(108, 66);
                    case Q: return new QRCodeCapacity(64, 80);
                    case H: return new QRCodeCapacity(48, 96);
                }
                break;
            case 10:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(136, 36);
                    case M: return new QRCodeCapacity(172, 104);
                    case Q: return new QRCodeCapacity(114, 144);
                    case H: return new QRCodeCapacity(90, 168);
                }
                break;
            case 11:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(324, 80);
                    case M: return new QRCodeCapacity(50, 30);
                    case Q: return new QRCodeCapacity(88, 112);
                    case H: return new QRCodeCapacity(36, 72);
                }
                break;
            case 12:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(184, 48);
                    case M: return new QRCodeCapacity(216, 132);
                    case Q: return new QRCodeCapacity(80, 104);
                    case H: return new QRCodeCapacity(98, 196);
                }
                break;
            case 13:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(428, 104);
                    case M: return new QRCodeCapacity(296, 176);
                    case Q: return new QRCodeCapacity(160, 192);
                    case H: return new QRCodeCapacity(132, 264);
                }
                break;
            case 14:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(345, 90);
                    case M: return new QRCodeCapacity(160, 96);
                    case Q: return new QRCodeCapacity(176, 220);
                    case H: return new QRCodeCapacity(132, 264);
                }
                break;
            case 15:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(435, 110);
                    case M: return new QRCodeCapacity(205, 120);
                    case Q: return new QRCodeCapacity(120, 150);
                    case H: return new QRCodeCapacity(132, 264);
                }
                break;
            case 16:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(490, 120);
                    case M: return new QRCodeCapacity(315, 196);
                    case Q: return new QRCodeCapacity(285, 360);
                    case H: return new QRCodeCapacity(45, 90);
                }
                break;
            case 17:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(107, 28);
                    case M: return new QRCodeCapacity(460, 280);
                    case Q: return new QRCodeCapacity(22, 28);
                    case H: return new QRCodeCapacity(28, 56);
                }
                break;
            case 18:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(600, 150);
                    case M: return new QRCodeCapacity(387, 234);
                    case Q: return new QRCodeCapacity(374, 476);
                    case H: return new QRCodeCapacity(28, 56);
                }
                break;
            case 19:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(339, 84);
                    case M: return new QRCodeCapacity(132, 78);
                    case Q: return new QRCodeCapacity(357, 442);
                    case H: return new QRCodeCapacity(117, 234);
                }
                break;
            case 20:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(321, 84);
                    case M: return new QRCodeCapacity(123, 78);
                    case Q: return new QRCodeCapacity(360, 450);
                    case H: return new QRCodeCapacity(225, 420);
                }
                break;
            case 21:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(464, 112);
                    case M: return new QRCodeCapacity(714, 442);
                    case Q: return new QRCodeCapacity(374, 476);
                    case H: return new QRCodeCapacity(304, 570);
                }
                break;
            case 22:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(222, 56);
                    case M: return new QRCodeCapacity(782, 476);
                    case Q: return new QRCodeCapacity(168, 210);
                    case H: return new QRCodeCapacity(442, 816);
                }
                break;
            case 23:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(484, 120);
                    case M: return new QRCodeCapacity(188, 112);
                    case Q: return new QRCodeCapacity(264, 330);
                    case H: return new QRCodeCapacity(240, 480);
                }
                break;
            case 24:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(702, 180);
                    case M: return new QRCodeCapacity(270, 168);
                    case Q: return new QRCodeCapacity(264, 330);
                    case H: return new QRCodeCapacity(480, 900);
                }
                break;
            case 25:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(848, 208);
                    case M: return new QRCodeCapacity(376, 224);
                    case Q: return new QRCodeCapacity(168, 210);
                    case H: return new QRCodeCapacity(330, 660);
                }
                break;
            case 26:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(1140, 280);
                    case M: return new QRCodeCapacity(874, 532);
                    case Q: return new QRCodeCapacity(616, 784);
                    case H: return new QRCodeCapacity(528, 990);
                }
                break;
            case 27:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(976, 240);
                    case M: return new QRCodeCapacity(990, 616);
                    case Q: return new QRCodeCapacity(184, 240);
                    case H: return new QRCodeCapacity(180, 360);
                }
                break;
            case 28:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(351, 90);
                    case M: return new QRCodeCapacity(135, 84);
                    case Q: return new QRCodeCapacity(96, 120);
                    case H: return new QRCodeCapacity(165, 330);
                }
                break;
            case 29:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(812, 210);
                    case M: return new QRCodeCapacity(945, 588);
                    case Q: return new QRCodeCapacity(23, 30);
                    case H: return new QRCodeCapacity(285, 570);
                }
                break;
            case 30:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(575, 150);
                    case M: return new QRCodeCapacity(893, 532);
                    case Q: return new QRCodeCapacity(360, 450);
                    case H: return new QRCodeCapacity(345, 690);
                }
                break;
            case 31:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(1495, 390);
                    case M: return new QRCodeCapacity(92, 56);
                    case Q: return new QRCodeCapacity(1008, 1260);
                    case H: return new QRCodeCapacity(345, 690);
                }
                break;
            case 32:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(1955, 510);
                    case M: return new QRCodeCapacity(460, 280);
                    case Q: return new QRCodeCapacity(240, 300);
                    case H: return new QRCodeCapacity(285, 570);
                }
                break;
            case 33:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(1955, 510);
                    case M: return new QRCodeCapacity(644, 392);
                    case Q: return new QRCodeCapacity(696, 870);
                    case H: return new QRCodeCapacity(165, 330);
                }
                break;
            case 34:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(1495, 390);
                    case M: return new QRCodeCapacity(644, 392);
                    case Q: return new QRCodeCapacity(1056, 1320);
                    case H: return new QRCodeCapacity(944, 1770);
                }
                break;
            case 35:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(1452, 360);
                    case M: return new QRCodeCapacity(564, 336);
                    case Q: return new QRCodeCapacity(936, 1170);
                    case H: return new QRCodeCapacity(330, 660);
                }
                break;
            case 36:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(726, 180);
                    case M: return new QRCodeCapacity(282, 168);
                    case Q: return new QRCodeCapacity(1104, 1380);
                    case H: return new QRCodeCapacity(30, 60);
                }
                break;
            case 37:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(2074, 510);
                    case M: return new QRCodeCapacity(1334, 812);
                    case Q: return new QRCodeCapacity(1176, 1470);
                    case H: return new QRCodeCapacity(360, 720);
                }
                break;
            case 38:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(488, 120);
                    case M: return new QRCodeCapacity(598, 364);
                    case Q: return new QRCodeCapacity(1152, 1440);
                    case H: return new QRCodeCapacity(630, 1260);
                }
                break;
            case 39:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(2340, 600);
                    case M: return new QRCodeCapacity(1880, 1120);
                    case Q: return new QRCodeCapacity(1032, 1290);
                    case H: return new QRCodeCapacity(150, 300);
                }
                break;
            case 40:
                switch (eccLevel) {
                    case L: return new QRCodeCapacity(2242, 570);
                    case M: return new QRCodeCapacity(846, 504);
                    case Q: return new QRCodeCapacity(816, 1020);
                    case H: return new QRCodeCapacity(300, 600);
                }
                break;
        }
        throw new IllegalArgumentException("Unsupported version or ECC level");
    }
}
