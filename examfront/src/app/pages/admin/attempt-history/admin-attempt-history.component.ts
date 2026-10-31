import { Component, OnInit } from '@angular/core';
import { QuestionService } from 'src/app/services/question.service';

@Component({
  selector: 'app-admin-attempt-history',
  templateUrl: './admin-attempt-history.component.html',
  styleUrls: ['./admin-attempt-history.component.css']
})
export class AdminAttemptHistoryComponent implements OnInit {
  attempts: any[] = [];
  loading = true;
  loadError = false;

  constructor(private questionService: QuestionService) {}

  ngOnInit(): void {
    this.questionService.getAllAttempts().subscribe({
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