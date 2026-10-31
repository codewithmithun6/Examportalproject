import { Component, OnInit } from '@angular/core';
import { QuestionService } from 'src/app/services/question.service';

@Component({
  selector: 'app-attempt-history',
  templateUrl: './attempt-history.component.html',
  styleUrls: ['./attempt-history.component.css']
})
export class AttemptHistoryComponent implements OnInit {
  attempts: any[] = [];
  selectedAttempt: any = null;
  loading = true;
  loadError = false;
  private previousDocumentTitle = '';

  constructor(private questionService: QuestionService) {}

  ngOnInit(): void {
    this.questionService.getMyAttempts().subscribe({
      next: (attempts) => {
        this.attempts = attempts;
        this.loading = false;
      },
      error: () => {
        this.loadError = true;
        this.loading = false;
      }
    });
  }

  exportResult(attempt: any): void {
    this.selectedAttempt = attempt;
    this.previousDocumentTitle = document.title;
    document.title = `Quiz Result - ${attempt.quizTitle || 'Exam Portal'}`;
    document.body.classList.add('printing-quiz-result');

    window.addEventListener('afterprint', () => {
      document.body.classList.remove('printing-quiz-result');
      document.title = this.previousDocumentTitle;
      this.selectedAttempt = null;
    }, { once: true });

    window.setTimeout(() => window.print(), 0);
  }
}