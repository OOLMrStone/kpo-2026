package studying.pos.application;

import studying.pos.domain.FinalScore;
import studying.pos.domain.Student;

public record ResultRow(Student student, FinalScore score) { }
