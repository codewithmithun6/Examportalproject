import { Component, OnInit } from '@angular/core';
import { QuestionService } from 'src/app/services/question.service';

@Component({
  selector: 'app-attempt-history',
  templateUrl: './attempt-history.component.html',
  styleUrls: ['./attempt-history.component.css']
})
export class AttemptHistoryComponent implements OnInit {
  attempts: any[] = [];
  loading = true;
  loadError = false;

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
}